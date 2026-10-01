package itspadar.voicechatutils.commands;

import de.maxhenkel.voicechat.api.VoicechatConnection;
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
import java.util.UUID;

import static itspadar.voicechatutils.SimpleVoiceChatAPI.API;
import static itspadar.voicechatutils.VoicechatUtils.*;

public class MGToggleCommand implements CommandExecutor, TabCompleter {

    private static final HashSet<UUID> MessageGroupToggledOn = new HashSet<>();

    public static boolean hasMessageGroupToggledOn(UUID player) {
        return MessageGroupToggledOn.contains(player);
    }

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

        if (!CONFIG.enable_messagegroup) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>This command is disabled in the config!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }
        if (!(sender.hasPermission("voicechatutils.chat.use") || sender.hasPermission("voicechat.speak"))) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You must have both voicechat.speak and voicechatutil.chat.use permissions to use this command!",
                    Placeholder.component("prefix", PREFIX)
            ));
            return true;
        }

        if (args.length == 0) {
            if (MessageGroupToggledOn.contains(player.getUniqueId())) {
                MessageGroupToggledOn.remove(player.getUniqueId());
            } else {
                MessageGroupToggledOn.add(player.getUniqueId());
            }
        } else if (args.length == 1) {
            if (args[0].equals("on")) {
                MessageGroupToggledOn.add(player.getUniqueId());
            } else if (args[0].equals("off")) {
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

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("", "on", "off");
    }
}
