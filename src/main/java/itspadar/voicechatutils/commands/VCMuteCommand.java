package itspadar.voicechatutils.commands;

import itspadar.voicechatutils.MuteManager;
import itspadar.voicechatutils.MuteManager.Mute;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static itspadar.voicechatutils.VoicechatUtils.*;

/**
 * Handles /vcmute, /vcunmute and /vcmutes.
 */
public class VCMuteCommand implements CommandExecutor, TabCompleter {
    private static final @NotNull String PERMISSION = "voicechatutils.mute";
    private static final @NotNull List<String> DURATION_SUGGESTIONS = List.of("10m", "30m", "1h", "1d", "7d", "permanent");

    /**
     * Sends a message to the sender, every online player with the mute permission, and the console.
     */
    private static void announce(@NotNull CommandSender sender, @NotNull Component announcement) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sender && player.hasPermission(PERMISSION)) {
                player.sendMessage(announcement);
            }
        }
        sender.sendMessage(announcement);
        if (!(sender instanceof ConsoleCommandSender)) {
            COMPONENTLOGGER.info(announcement);
        }
    }

    private static void reply(@NotNull CommandSender sender, @NotNull String text, @NotNull String name) {
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<prefix> " + text,
                Placeholder.component("prefix", PREFIX),
                Placeholder.unparsed("name", name)
        ));
    }

    private static @Nullable OfflinePlayer findPlayer(@NotNull String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayerIfCached(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return switch (command.getName()) {
            case "vcmute" -> mute(sender, args);
            case "vcunmute" -> unmute(sender, args);
            default -> list(sender);
        };
    }

    private boolean mute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }

        OfflinePlayer target = findPlayer(args[0]);
        if (target == null) {
            reply(sender, "<red>Could not find a player called <name>!", args[0]);
            return true;
        }

        long until = MuteManager.PERMANENT;
        int reasonStart = 1;
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("perm") || args[1].equalsIgnoreCase("permanent")) {
                reasonStart = 2;
            } else {
                long duration = MuteManager.parseDuration(args[1]);
                if (duration > 0) {
                    until = System.currentTimeMillis() + duration;
                    reasonStart = 2;
                }
            }
        }
        String reason = args.length > reasonStart ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length)) : null;

        String name = target.getName() != null ? target.getName() : args[0];
        Mute mute = new Mute(target.getUniqueId(), name, until, reason, sender.getName());
        MUTES.mute(mute);

        Component reasonText = Component.text(reason == null ? "" : " Reason: " + reason);
        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You have been muted in voicechat <duration>.<reason>",
                    Placeholder.component("prefix", PREFIX),
                    Placeholder.unparsed("duration", mute.isPermanent() ? "permanently" : "for " + mute.remaining()),
                    Placeholder.component("reason", reasonText)
            ));
        }

        Component announcement = MINI_MESSAGE.deserialize(
                "<prefix> <yellow><name></yellow> was muted in voicechat by <yellow><sender></yellow> <duration>.<reason>",
                Placeholder.component("prefix", PREFIX),
                Placeholder.unparsed("name", name),
                Placeholder.unparsed("sender", sender.getName()),
                Placeholder.unparsed("duration", mute.isPermanent() ? "permanently" : "for " + mute.remaining()),
                Placeholder.component("reason", reasonText)
        );
        announce(sender, announcement);
        return true;
    }

    private boolean unmute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            return false;
        }

        // Prefer an existing mute with this name so players that aren't cached can still be unmuted
        Mute existing = MUTES.getMutes().stream()
                .filter(mute -> mute.name().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);
        Mute removed = null;
        if (existing != null) {
            removed = MUTES.unmute(existing.player());
        } else {
            OfflinePlayer target = findPlayer(args[0]);
            if (target != null) {
                removed = MUTES.unmute(target.getUniqueId());
            }
        }

        if (removed == null) {
            reply(sender, "<red><name> is not muted in voicechat!", args[0]);
            return true;
        }

        Player online = Bukkit.getPlayer(removed.player());
        if (online != null) {
            online.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <green>You have been unmuted in voicechat.",
                    Placeholder.component("prefix", PREFIX)
            ));
        }

        Component announcement = MINI_MESSAGE.deserialize(
                "<prefix> <yellow><name></yellow> was unmuted in voicechat by <yellow><sender></yellow>.",
                Placeholder.component("prefix", PREFIX),
                Placeholder.unparsed("name", removed.name()),
                Placeholder.unparsed("sender", sender.getName())
        );
        announce(sender, announcement);
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean list(@NotNull CommandSender sender) {
        List<Mute> mutes = MUTES.getMutes();
        if (mutes.isEmpty()) {
            reply(sender, "<green>Nobody is voicechat muted.", "");
            return true;
        }
        reply(sender, "<white><name> muted in voicechat:", mutes.size() + (mutes.size() == 1 ? " player is" : " players are"));
        mutes.forEach(mute -> sender.sendMessage(Component.text(" - ").append(MuteManager.describe(mute))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1 && !(command.getName().equals("vcmute") && args.length == 2)) {
            return List.of();
        }
        Stream<String> options;
        if (args.length == 2) {
            options = DURATION_SUGGESTIONS.stream();
        } else if (command.getName().equals("vcunmute")) {
            options = MUTES.getMutes().stream().map(Mute::name);
        } else if (command.getName().equals("vcmute")) {
            options = Bukkit.getOnlinePlayers().stream().map(Player::getName);
        } else {
            return List.of();
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
