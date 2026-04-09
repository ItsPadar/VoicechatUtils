package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleVoiceChatAPI implements VoicechatPlugin {
    public static VoicechatServerApi API;

    public static boolean isInGroup(UUID playerid, UUID groupid) {
        VoicechatConnection connection = API.getConnectionOf(playerid);
        if (connection != null) {
            Group group = connection.getGroup();
            if (group != null) {
                return group.getId() == groupid;
            }
        }

        return false;
    }

    public static List<Player> getPlayersInGroup(UUID groupid) {
        List<Player> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInGroup(player.getUniqueId(), groupid)) {
                result.add(player);
            }
        }
        return result;
    }

    @Override
    public String getPluginId() {
        return "voicechatutils";
    }

    @Override
    public void registerEvents(EventRegistration register) {
        register.registerEvent(VoicechatServerStartedEvent.class, this::serverStart);
    }

    public void serverStart(VoicechatServerStartedEvent event) {
        API = event.getVoicechat();
    }
}