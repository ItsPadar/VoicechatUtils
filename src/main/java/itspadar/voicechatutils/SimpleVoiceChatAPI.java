package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static itspadar.voicechatutils.VoicechatUtils.*;
import static org.bukkit.Bukkit.getOnlinePlayers;

public class SimpleVoiceChatAPI implements VoicechatPlugin {
    static final Map<Player, Group> playerGroupMap = new HashMap<>();
    public static @Nullable VoicechatServerApi API;

    static void playerTick() {
        if (API != null) {
            Collection<? extends Player> players = getOnlinePlayers();

            playerGroupMap.keySet().removeIf(player -> {
                if (!players.contains(player)) {
//                    LOGGER.info("removed "+player.getName()+": "+playerGroupMap.get(player).getName());
                    leaveGroupEvent(player, playerGroupMap.get(player));
                    return true;
                }
                return false;
            });

            for (Player player : players) {
                VoicechatConnection connection = API.getConnectionOf(player.getUniqueId());
                if (connection != null) {
                    Group oldGroup = playerGroupMap.get(player);
                    UUID oldGroupID;
                    if (oldGroup != null) {
                        oldGroupID = oldGroup.getId();
                    } else {
                        oldGroupID = null;
                    }

                    Group newGroup = connection.getGroup();
                    UUID newGroupID;
                    if (newGroup != null) {
                        newGroupID = newGroup.getId();
                    } else {
                        newGroupID = null;
                    }

                    if (!Objects.equals(newGroupID, oldGroupID)) {
                        if (oldGroup != null) {
                            // leave event
                            leaveGroupEvent(player, oldGroup);
                        }
                        if (newGroup != null) {
                            // join event
                            joinGroupEvent(player, newGroup);
                        }

                        if (newGroup == null) {
                            playerGroupMap.remove(player);
                            // LOGGER.log("removed "+player.getName());
                        } else {
                            playerGroupMap.put(player, newGroup);
                        }
                    }
                }

            }
        }
    }

    static void joinGroupEvent(@NotNull Player player, @NotNull Group group) {
        Component message = MINI_MESSAGE.deserialize(
                CONFIG.join_group_message_text,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.component("name", player.displayName()),
                Placeholder.parsed("prefix", prefixSuffix.getPrefix(player)),
                Placeholder.parsed("suffix", prefixSuffix.getSuffix(player))
        );
        LOGGER.info(player.getName() + " joined group " + group.getName());
        if (CONFIG.join_leave_group_messages_mode.equals("actionbar")) {
            getPlayersInGroup(group.getId()).forEach(person -> person.sendActionBar(message));
        } else {
            getPlayersInGroup(group.getId()).forEach(person -> person.sendMessage(message));
        }
    }

    static void leaveGroupEvent(@NotNull Player player, @NotNull Group group) {
        Component message = MINI_MESSAGE.deserialize(
                CONFIG.leave_group_message_text,
                Placeholder.unparsed("group", group.getName()),
                Placeholder.component("name", player.displayName()),
                Placeholder.parsed("prefix", prefixSuffix.getPrefix(player)),
                Placeholder.parsed("suffix", prefixSuffix.getSuffix(player))
        );
        LOGGER.info(player.getName() + " left group " + group.getName());
        if (CONFIG.join_leave_group_messages_mode.equals("actionbar")) {
            getPlayersInGroup(group.getId()).forEach(person -> person.sendActionBar(message));
            player.sendActionBar(message);
        } else {
            getPlayersInGroup(group.getId()).forEach(person -> person.sendMessage(message));
            player.sendMessage(message);
        }


    }

    public static boolean isInGroup(@NotNull UUID playerid, @NotNull UUID groupid) {
        if (API == null) {
            return false;
        }
        VoicechatConnection connection = API.getConnectionOf(playerid);
        if (connection != null) {
            Group group = connection.getGroup();
            if (group != null) {
                return group.getId().equals(groupid);
            }
        }

        return false;
    }

    public static List<Player> getPlayersInGroup(UUID groupid) {
        List<Player> result = new ArrayList<>();
        for (Player player : getOnlinePlayers()) {
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
        register.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        LOGGER.info("Registered events");
    }

    public void serverStart(VoicechatServerStartedEvent event) {
        API = event.getVoicechat();
    }

    /**
     * Drops audio from voice muted players before it reaches anyone. Runs on the voice chat thread.
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null) {
            return;
        }
        MuteManager.Mute mute = MUTES.getMute(sender.getPlayer().getUuid());
        if (mute != null) {
            event.cancel();
            MUTES.notifyMutedSpeaker(mute);
        }
    }
}