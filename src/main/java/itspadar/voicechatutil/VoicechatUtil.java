package itspadar.voicechatutil;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class VoicechatUtil extends JavaPlugin {
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final Component PREFIX = MINI_MESSAGE.deserialize("<click:open_url:'https://modrinth.com/project/voicechat-util'><hover:show_text:'https://modrinth.com/project/voicechat-util'>[<blue>Voicechat Utils</blue>]</hover></click>");
    public static Logger LOGGER;
    public static ComponentLogger COMPONENTLOGGER;
    public static FileConfiguration CONFIG;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        COMPONENTLOGGER = getComponentLogger();

        saveDefaultConfig();
        CONFIG = getConfig();

        getCommand("messagegroup").setExecutor(new MessageGroupCommand());

        // register voice chat plugins
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(new SimpleVoiceChatAPI());
        }

        getLogger().info("Loaded!");
    }
}
