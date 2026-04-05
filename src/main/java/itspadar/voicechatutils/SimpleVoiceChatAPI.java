package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static itspadar.voicechatutils.VoicechatUtils.CONFIG;
import static itspadar.voicechatutils.VoicechatUtils.MINI_MESSAGE;

public class SimpleVoiceChatAPI implements VoicechatPlugin {
    public static VoicechatServerApi API;

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
}