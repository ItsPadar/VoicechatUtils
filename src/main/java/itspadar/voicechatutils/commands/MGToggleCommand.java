package itspadar.voicechatutils.commands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static itspadar.voicechatutils.VoicechatUtils.*;

public class MGToggleCommand implements CommandExecutor, TabCompleter {

    private static final @NotNull HashSet<UUID> MessageGroupToggledOn = new HashSet<>();

    public static boolean hasMessageGroupToggledOn(UUID player) {
        return MessageGroupToggledOn.contains(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (MessageGroupCommand.checkCanUseMessageGroup(sender) == null) {
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            if (MessageGroupToggledOn.contains(player.getUniqueId())) {
                MessageGroupToggledOn.remove(player.getUniqueId());
            } else {
                MessageGroupToggledOn.add(player.getUniqueId());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                MessageGroupToggledOn.add(player.getUniqueId());
            } else if (args[0].equalsIgnoreCase("off")) {
                MessageGroupToggledOn.remove(player.getUniqueId());
            } else {
                return false;
            }

        } else {
            return false;
        }

        String state;
        if (hasMessageGroupToggledOn(player.getUniqueId())) {
            state = "<green>ON";
        } else {
            state = "<red>OFF";
        }
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<prefix> Chat MessageGroup toggled <bold><state></bold><white>!",
                Placeholder.component("prefix", PREFIX),
                Placeholder.parsed("state", state)
        ));

        COMPONENTLOGGER.info(MINI_MESSAGE.deserialize(
                "<player> toggled Chat MessageGroup <state>",
                Placeholder.unparsed("player", player.getName()),
                Placeholder.parsed("state", state)
        ));

        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            return List.of();
        }
        return Stream.of("on", "off").filter(option -> option.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }
}
