package itspadar.voicechatutil;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static itspadar.voicechatutil.SimpleVoiceChatAPI.API;
import static itspadar.voicechatutil.VoicechatUtil.*;

public class MessageGroupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        VoicechatConnection connection = API.getConnectionOf(player.getUniqueId());

        if (connection == null) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You must have Simple Voice Chat installed to use this command!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }
        if (!CONFIG.getBoolean("enable_messagegroup")) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>This command is disabled in the config!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }
        if (!(sender.hasPermission("voicechatutil.chat.use") || sender.hasPermission("voicechat.speak"))) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You must have both voicechat.speak and voicechatutil.chat.use permissions to use this command!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }
        Group group = connection.getGroup();
        if (group == null) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You must be in a group to use this command!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        Component message = MINI_MESSAGE.deserialize(
                CONFIG.getString("messagegroup_text", "<gray>[<group>]</gray> <<name>> <message>"),
                Placeholder.unparsed("group", group.getName()),
                Placeholder.component("name", player.displayName()),
                Placeholder.unparsed("message", String.join(" ", args))
        );

        UUID groupID = group.getId();
        for (Player user : (player.getWorld().getPlayers())) {
            VoicechatConnection userconnection = API.getConnectionOf(user.getUniqueId());
            if (user.hasPermission("nomicchat.chat.spy") && CONFIG.getBoolean("enable_messagegroup_spying", false)) {
                user.sendMessage(message);
                continue;
            }
            if (userconnection != null) {
                Group playergroup = userconnection.getGroup();
                if (playergroup != null) {
                    if (playergroup.getId() == groupID) {
                        user.sendMessage(message);
                    }
                }
            }
        }

        COMPONENTLOGGER.info(message);

        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("");
    }
}
