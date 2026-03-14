package itspadar.voicechatutil;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class SimpleVoiceChatAPI implements VoicechatPlugin {
    public static VoicechatServerApi API;

    @Override
    public String getPluginId() {
        return "voicechatutil";
    }

    @Override
    public void registerEvents(EventRegistration register) {
        register.registerEvent(VoicechatServerStartedEvent.class, this::serverStart);
    }

    public void serverStart(VoicechatServerStartedEvent event) {
        API = event.getVoicechat();
    }

//    public static boolean isPlayerInGroup(UUID player, UUID groupID) {
//        try {
//            return API.getConnectionOf(player).getGroup().getId().equals(groupID);
//        } catch (NullPointerException ignore) {
//            return false;
//        }
//    }
//
//    @Nullable
//    public static Group getPlayerGroup(UUID player) {
//        try {
//            var connection = API.getConnectionOf(player);
//
//            var group = connection.getGroup();
//        } catch (NullPointerException ignore) {
//            return null;
//        }
//    }
//
//    @Nullable
//    public static ArrayList<Player> groupPlayers(UUID groupUUID, World world) {
//        try {
//            ArrayList<Player> list = new ArrayList<>();
//            for (Player player : world.getPlayers()) {
//                if (isPlayerInGroup(player.getUniqueId(), groupUUID)) {
//                    list.add(player);
//                }
//            }
//            return list;
//        } catch (NullPointerException ignore) {
//            return null;
//        }
//    }
}