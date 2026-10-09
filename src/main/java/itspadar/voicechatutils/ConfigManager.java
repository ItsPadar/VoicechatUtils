package itspadar.voicechatutils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static itspadar.voicechatutils.VoicechatUtils.LOGGER;

public class ConfigManager {
    final int CONFIG_VERSION = 4;

    public Boolean enable_messagegroup;
    public Boolean enable_messagegroup_spying;
    public String messagegroup_text;
    public String join_leave_group_messages_mode;
    public String leave_group_message_text;
    public String join_group_message_text;
    public Boolean enable_prefix_suffix_support;
    public String prefix_suffix_colour_mode;
    public Boolean voice_mute_blocks_messagegroup;

    public ConfigManager(@NotNull VoicechatUtils plugin) {
        plugin.saveDefaultConfig();

        File currentConfigFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration currentConfig = new YamlConfiguration();
        currentConfig.options().parseComments(true);


        try {
            currentConfig.load(currentConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Exception occurred while loading config.yml file!", e);
        }

        int configver = currentConfig.getInt("CONFIG_VERSION", 0);
        if (configver == 0) {
            LOGGER.info("Found unknown config.yml version. This is likely due to an old version being updated or something weird.");
        } else {
            LOGGER.info("Found config.yml version " + configver);
        }

        if (configver > CONFIG_VERSION) {
            throw new RuntimeException("Config.yml CONFIG_VERSION variable is  " + configver +
                    " which is bigger than the plugin's internal config.yml version " + CONFIG_VERSION +
                    "! Please ensure you have the correct VoicechatUtils version! Set CONFIG_VERSION to 0 or remove config.yml to override this warning."
            );
        } else if (configver < CONFIG_VERSION) {
            if (configver < 2) {
                for (String key : List.of("messagegroup_text", "join_group_message_text", "leave_group_message_text")) {
                    String value = currentConfig.getString(key);
                    if (value != null) {
                        if (value.contains("<<name>>")) {
                            currentConfig.set(key, value
                                    .replaceFirst("<<name>>", "<prefix><<name>><suffix>")
                            );
                        } else {
                            currentConfig.set(key, value
                                    .replaceFirst("<name>", "<prefix><name><suffix>")
                            );
                        }
                    }
                }
                //noinspection UnusedAssignment
                configver = 2;
            }
            // if per version migration logic is necessary then it would go here
            LOGGER.info("Attempting config.yml upgrade to " + CONFIG_VERSION);

            YamlConfiguration originalConfig = new YamlConfiguration();
            originalConfig.options().parseComments(true);

            try {
                originalConfig.loadFromString(
                        new BufferedReader(
                                new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n")
                                )
                );
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException("Exception occurred while loading config.yml file!", e);
            }

            int ignored_num = 0;
            int replaced_num = 0;
            for (String key : originalConfig.getKeys(true)) {
                // check key exists in current config, is not the same in both, and that it is not CONFIG_VERSION
                Object value = currentConfig.get(key);
                Object originalValue = originalConfig.get(key);
                if ((!key.equals("CONFIG_VERSION")) && (value != null) && !Objects.equals(value, originalValue)) {
                    originalConfig.set(key, value);
                    replaced_num++;
                    LOGGER.finest("Replaced " + key + " from `" + originalValue + "` to `" + value + "`");
                } else {
                    ignored_num++;
                    LOGGER.finest("Ignored " + key);
                }
            }

            try {
                originalConfig.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            currentConfig = originalConfig;

            LOGGER.info("Updated config.yml to version " + CONFIG_VERSION + " with " + replaced_num + " non default values converted and " + ignored_num + " ignored values!");
        }

        enable_messagegroup = currentConfig.getBoolean("enable_messagegroup");
        enable_messagegroup_spying = currentConfig.getBoolean("enable_messagegroup_spying");
        messagegroup_text = currentConfig.getString("messagegroup_text");
        join_leave_group_messages_mode = currentConfig.getString("join_leave_group_messages_mode");
        switch (join_leave_group_messages_mode) {
            case "chat", "actionbar", "disable":
                break;
            case null, default:
                LOGGER.warning("Invalid value for join_leave_group_messages_mode, defaulting to chat");
                join_leave_group_messages_mode = "chat";
        }
        leave_group_message_text = currentConfig.getString("leave_group_message_text");
        join_group_message_text = currentConfig.getString("join_group_message_text");
        enable_prefix_suffix_support = currentConfig.getBoolean("enable_prefix_suffix_support");
        prefix_suffix_colour_mode = currentConfig.getString("prefix_suffix_colour_mode");
        switch (prefix_suffix_colour_mode) {
            case "automatic", "minimessage", "legacy":
                break;
            case null, default:
                LOGGER.warning("Invalid value for prefix_suffix_colour_mode, defaulting to automatic");
                prefix_suffix_colour_mode = "automatic";
        }

        voice_mute_blocks_messagegroup = currentConfig.getBoolean("voice_mute_blocks_messagegroup", true);

        LOGGER.info("Finished loading config.yml");
    }
}
