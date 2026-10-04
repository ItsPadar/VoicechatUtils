package itspadar.voicechatutils.commands;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import itspadar.voicechatutils.MuteManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static itspadar.voicechatutils.SimpleVoiceChatAPI.API;
import static itspadar.voicechatutils.SimpleVoiceChatAPI.isInGroup;
import static itspadar.voicechatutils.VoicechatUtils.*;

public class MessageGroupCommand implements CommandExecutor, TabCompleter {

    public static void sendMessageGroupMessage(Group group, Player player, String mg) {
        Component message = MINI_MESSAGE.deserialize(
                CONFIG.messagegroup_text,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.parsed("prefix", prefixSuffix.getPrefix(player)),
                Placeholder.parsed("suffix", prefixSuffix.getSuffix(player)),
                Placeholder.component("name", player.displayName()),
                Placeholder.unparsed("message", mg)
        );

        UUID groupID = group.getId();
        for (Player user : Bukkit.getOnlinePlayers()) {
            if (user.hasPermission("voicechatutils.chat.spy") && CONFIG.enable_messagegroup_spying) {
                user.sendMessage(message);
                continue;
            }
            if (isInGroup(user.getUniqueId(), groupID)) {
                user.sendMessage(message);
            }
        }

        COMPONENTLOGGER.info(Component.text("[mg] ").append(message));
    }

    static void sendError(CommandSender sender, String text) {
        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<prefix> <red>" + text,
                Placeholder.component("prefix", PREFIX)
        ));
    }

    /**
     * Checks everything a player needs to use /mg or /mgtoggle, telling them what is wrong if something is.
     *
     * @return the player's voicechat connection, or null if they cannot use message group commands
     */
    static @Nullable VoicechatConnection checkCanUseMessageGroup(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return null;
        }
        if (!CONFIG.enable_messagegroup) {
            sendError(sender, "This command is disabled in the config!");
            return null;
        }
        if (!(sender.hasPermission("voicechatutils.chat.use") && sender.hasPermission("voicechat.speak"))) {
            sendError(sender, "You must have both voicechat.speak and voicechatutils.chat.use permissions to use this command!");
            return null;
        }
        if (API == null) {
            sendError(sender, "Simple Voice Chat has not started yet!");
            return null;
        }
        VoicechatConnection connection = API.getConnectionOf(player.getUniqueId());
        if (connection == null) {
            sendError(sender, "You must have Simple Voice Chat installed to use this command!");
            return null;
        }
        return connection;
    }

    /**
     * @return true (and tells the player why) if the player is voice muted and voice mutes also block /mg
     */
    public static boolean isBlockedByMute(Player player) {
        MuteManager.Mute mute = MUTES.getMute(player.getUniqueId());
        if (CONFIG.voice_mute_blocks_messagegroup && mute != null) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You can't send group messages while you are muted in voicechat <duration>!",
                    Placeholder.component("prefix", PREFIX),
                    Placeholder.unparsed("duration", mute.isPermanent() ? "permanently" : "for " + mute.remaining())
            ));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        VoicechatConnection connection = checkCanUseMessageGroup(sender);
        if (connection == null) {
            return true;
        }
        Player player = (Player) sender;

        Group group = connection.getGroup();
        if (group == null) {
            sendError(sender, "You must be in a group to use this command!");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        if (isBlockedByMute(player)) {
            return true;
        }

        sendMessageGroupMessage(group, player, String.join(" ", args));

        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
