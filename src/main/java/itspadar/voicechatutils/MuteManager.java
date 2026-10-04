package itspadar.voicechatutils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static itspadar.voicechatutils.VoicechatUtils.*;

/**
 * Keeps track of players that are muted in voicechat. Mutes are stored in mutes.yml so they survive restarts.
 * <p>
 * {@link #getMute(UUID)} is called from the voicechat thread for every microphone packet, so the mute map is concurrent.
 * Everything else (commands, saving, expiry) runs on the main server thread.
 */
public class MuteManager {
    public static final long PERMANENT = -1;
    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)([smhdw])");
    private static final long MUTED_NOTICE_COOLDOWN_MS = 3000;
    private final VoicechatUtils plugin;
    private final File file;
    private final Map<UUID, Mute> mutes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMutedNotice = new ConcurrentHashMap<>();

    public MuteManager(VoicechatUtils plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
        load();
        Bukkit.getScheduler().runTaskTimer(plugin, this::expireMutes, 20, 20);
    }

    /**
     * Parses durations such as "30s", "10m", "2h", "7d", "1w" or combinations like "1h30m".
     *
     * @return the duration in milliseconds, or -1 if the input is not a valid duration
     */
    public static long parseDuration(@NotNull String input) {
        String text = input.toLowerCase(Locale.ROOT);
        Matcher matcher = DURATION_PART.matcher(text);
        long total = 0;
        int end = 0;
        while (matcher.find()) {
            if (matcher.start() != end) {
                return -1;
            }
            end = matcher.end();
            long unit = switch (matcher.group(2)) {
                case "s" -> 1000L;
                case "m" -> 60_000L;
                case "h" -> 3_600_000L;
                case "d" -> 86_400_000L;
                default -> 604_800_000L; // w
            };
            try {
                total = Math.addExact(total, Math.multiplyExact(Long.parseLong(matcher.group(1)), unit));
            } catch (ArithmeticException | NumberFormatException e) {
                return -1;
            }
        }
        // also reject durations so long that adding them to the current time would overflow
        if (end == 0 || end != text.length() || total <= 0 || total > Long.MAX_VALUE / 2) {
            return -1;
        }
        return total;
    }

    public static String formatDuration(long millis) {
        long seconds = Math.max(1, (millis + 999) / 1000);
        long days = seconds / 86400;
        long hours = seconds % 86400 / 3600;
        long minutes = seconds % 3600 / 60;
        long secs = seconds % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        if (secs > 0 && days == 0) builder.append(secs).append("s");
        return builder.toString().trim();
    }

    public static Component describe(@NotNull Mute mute) {
        return MINI_MESSAGE.deserialize(
                "<yellow><name></yellow> <gray>-</gray> <duration> <gray>(</gray>by <mutedby><separator><reason><gray>)</gray>",
                Placeholder.unparsed("name", mute.name()),
                Placeholder.unparsed("duration", mute.isPermanent() ? "permanent" : mute.remaining() + " left"),
                Placeholder.parsed("separator", mute.reason() == null ? "" : " <gray>-</gray> "),
                Placeholder.unparsed("reason", mute.reason() == null ? "" : mute.reason()),
                Placeholder.unparsed("mutedby", mute.mutedBy())
        );
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            try {
                UUID uuid = UUID.fromString(key);
                mutes.put(uuid, new Mute(
                        uuid,
                        section.getString("name", key),
                        section.getLong("until", PERMANENT),
                        section.getString("reason"),
                        section.getString("muted_by", "Console")
                ));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Ignoring invalid UUID in mutes.yml: " + key);
            }
        }
        LOGGER.info("Loaded " + mutes.size() + " voicechat mutes");
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Mute mute : mutes.values()) {
            ConfigurationSection section = yaml.createSection(mute.player().toString());
            section.set("name", mute.name());
            section.set("until", mute.until());
            section.set("reason", mute.reason());
            section.set("muted_by", mute.mutedBy());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            LOGGER.severe("Failed to save mutes.yml: " + e.getMessage());
        }
    }

    /**
     * @return the active mute of this player, or null if they are not muted
     */
    public @Nullable Mute getMute(@NotNull UUID player) {
        Mute mute = mutes.get(player);
        if (mute == null || mute.isExpired()) {
            return null;
        }
        return mute;
    }

    public boolean isMuted(@NotNull UUID player) {
        return getMute(player) != null;
    }

    public void mute(@NotNull Mute mute) {
        mutes.put(mute.player(), mute);
        save();
    }

    /**
     * @return the mute that was removed, or null if the player was not muted
     */
    public @Nullable Mute unmute(@NotNull UUID player) {
        Mute mute = mutes.remove(player);
        if (mute != null) {
            save();
        }
        return mute == null || mute.isExpired() ? null : mute;
    }

    public List<Mute> getMutes() {
        return mutes.values().stream()
                .filter(mute -> !mute.isExpired())
                .sorted(Comparator.comparing(Mute::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Tells a muted player that nobody can hear them. Called from the voicechat thread, so it is rate limited
     * and the message itself is sent on the main thread.
     */
    public void notifyMutedSpeaker(@NotNull Mute mute) {
        long now = System.currentTimeMillis();
        Long last = lastMutedNotice.get(mute.player());
        if (last != null && now - last < MUTED_NOTICE_COOLDOWN_MS) {
            return;
        }
        lastMutedNotice.put(mute.player(), now);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(mute.player());
            if (player != null) {
                player.sendActionBar(MINI_MESSAGE.deserialize(
                        "<red>You are muted in voicechat " + (mute.isPermanent() ? "" : "for ") + "<remaining>",
                        Placeholder.unparsed("remaining", mute.remaining())
                ));
            }
        });
    }

    private void expireMutes() {
        boolean changed = mutes.values().removeIf(mute -> {
            if (!mute.isExpired()) {
                return false;
            }
            LOGGER.info(mute.name() + "'s voicechat mute has expired");
            Player player = Bukkit.getPlayer(mute.player());
            if (player != null) {
                player.sendMessage(MINI_MESSAGE.deserialize(
                        "<prefix> <green>Your voicechat mute has expired.",
                        Placeholder.component("prefix", PREFIX)
                ));
            }
            return true;
        });
        if (changed) {
            save();
        }
    }

    public record Mute(@NotNull UUID player, @NotNull String name, long until, @Nullable String reason,
                       @NotNull String mutedBy) {
        public boolean isPermanent() {
            return until == PERMANENT;
        }

        public boolean isExpired() {
            return !isPermanent() && System.currentTimeMillis() >= until;
        }

        public String remaining() {
            return isPermanent() ? "permanently" : formatDuration(until - System.currentTimeMillis());
        }
    }
}
