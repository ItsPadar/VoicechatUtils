package itspadar.voicechatutils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static itspadar.voicechatutils.VoicechatUtils.*;
import static org.bukkit.Bukkit.getServer;

public class PrefixSuffix implements Listener {
    private static final @NotNull LegacyComponentSerializer LEGACYCOMPONENT = LegacyComponentSerializer.legacyAmpersand();
    private @Nullable Chat vaultChat = null;

    public PrefixSuffix(@NotNull VoicechatUtils plugin) {
        if (CONFIG.enable_prefix_suffix_support == true) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                LOGGER.info("Vault API detected");
                refreshVault();
                getServer().getPluginManager().registerEvents(this, plugin);
            } else {
                LOGGER.info("Vault API not detected, prefix suffix support disabled");
                if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                    LOGGER.severe("LuckPerms installed but support is not available in this plugin as Vault, https://www.spigotmc.org/resources/vault.34315/ which provides an API for getting prefixes and suffixes, is not installed! \nPlease install Vault!");
                }
            }
        }
    }

    private @NotNull String convert(@NotNull String text) {
        if (CONFIG.prefix_suffix_colour_mode.equals("legacy") || (CONFIG.prefix_suffix_colour_mode.equals("automatic") && text.contains("&"))) {
            return MINI_MESSAGE.serialize(LEGACYCOMPONENT.deserialize(text));
        } else {
            return text;
        }
    }

    private void refreshVault() {
        Chat vaultChat = getServer().getServicesManager().load(Chat.class);
        if (vaultChat != this.vaultChat) {
            LOGGER.info("Vault Chat implementation registered: " + (vaultChat == null ? "null" : vaultChat.getName()));
        }
        this.vaultChat = vaultChat;
    }

    @EventHandler
    public void onServiceChange(@NotNull ServiceRegisterEvent event) {
        if (event.getProvider().getService() == Chat.class) {
            refreshVault();
        }
    }

    @EventHandler
    public void onServiceChange(@NotNull ServiceUnregisterEvent event) {
        if (event.getProvider().getService() == Chat.class) {
            refreshVault();
        }
    }

    public @NotNull String getPrefix(@NotNull Player player) {
        if (vaultChat != null) {
            @Nullable String data = vaultChat.getPlayerPrefix(player);
            if (data != null) {
                return convert(data);
            }
        }
        return "";
    }

    public @NotNull String getSuffix(@NotNull Player player) {
        if (vaultChat != null) {
            @Nullable String data = vaultChat.getPlayerSuffix(player);
            if (data != null) {
                return convert(data);
            }
        }
        return "";
    }
}
