package itspadar.voicechatutils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

import static itspadar.voicechatutils.VoicechatUtils.LOGGER;

public class ConfigManager {
    private static FileConfiguration config;

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    public String getString(String path) {
        return config.getString(path);
    }

    public final int CONFIG_VERSION = 1;
    public ConfigManager(VoicechatUtils plugin) {
        plugin.saveDefaultConfig();

        File currentConfigFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration currentConfig = new YamlConfiguration();
        currentConfig.options().parseComments(true);

        YamlConfiguration originalConfig = new YamlConfiguration();
        originalConfig.options().parseComments(true);

        try {
            currentConfig.load(currentConfigFile);
            originalConfig.loadFromString(
                    new BufferedReader(
                            new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")), StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n")
                            )
            );
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Exception occurred while loading configuration files!", e);
        }

        int configver = currentConfig.getInt("CONFIG_VERSION", 0);
        if (configver == 0) {
            LOGGER.info("Found unknown config version. This is likely due to an old version being updated or something weird.");
        } else {
            LOGGER.info("Found config version "+configver+" and attempting upgrade");
        }

        if (configver > CONFIG_VERSION) {
            throw new RuntimeException("Config.yml CONFIG_VERSION variable is  " + configver +
                    " which is bigger than the plugin's internal config version " + CONFIG_VERSION +
                    "! Please ensure you have the correct VoicechatUtils version! Set CONFIG_VERSION to 0 or remove config.yml to override this warning."
            );
        } else if (configver < CONFIG_VERSION) {
            // if per version migration logic is necessary then it would go here

            int ignored_num = 0;
            int replaced_num = 0;
            for (String key : originalConfig.getKeys(true)) {
                // check key exists in current config, is not the same in both, and that it is not CONFIG_VERSION
                Object value = currentConfig.get(key);
                Object originalValue = originalConfig.get(key);
                if ((!key.equals("CONFIG_VERSION")) && (value != null) && (value != originalValue)) {
                    originalConfig.set(key, value);
                    replaced_num++;
                    LOGGER.finest("Replaced "+key+" from `"+originalValue+"` to `"+value+"`");
                }
                else {
                    ignored_num++;
                    LOGGER.finest("Ignored "+key);
                }
            }

            try {
                originalConfig.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            LOGGER.info("Updated config to version "+CONFIG_VERSION+" with "+replaced_num+" non default values converted and "+ignored_num+" ignored values!");
        }

        config = originalConfig;
    }
}
