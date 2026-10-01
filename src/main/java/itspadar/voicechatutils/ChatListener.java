package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import itspadar.voicechatutils.commands.MGToggleCommand;
import itspadar.voicechatutils.commands.MessageGroupCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static itspadar.voicechatutils.SimpleVoiceChatAPI.API;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(@SuppressWarnings("deprecation") AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (API == null || !MGToggleCommand.hasMessageGroupToggledOn(player.getUniqueId())) {
            return;
        }
        VoicechatConnection connection = API.getConnectionOf(player.getUniqueId());
        if (connection == null) {
            return;
        }
        Group group = connection.getGroup();
        if (group == null) {
            return;
        }

        MessageGroupCommand.sendMessageGroupMessage(group, player, message);
        event.setCancelled(true);
    }
}