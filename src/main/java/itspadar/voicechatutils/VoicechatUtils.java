package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import itspadar.voicechatutils.commands.MessageGroupCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public class VoicechatUtils extends JavaPlugin {
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final Component PREFIX = MINI_MESSAGE.deserialize("<click:open_url:'https://modrinth.com/project/voicechat-utils'><hover:show_text:'https://modrinth.com/project/voicechat-utils'>[<blue>Voicechat Utils</blue>]</hover></click>");
    public static Logger LOGGER;
    public static ComponentLogger COMPONENTLOGGER;
    public static ConfigManager CONFIG;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        COMPONENTLOGGER = getComponentLogger();

        // run config upgrade if necessary, needs to run before everything else
        CONFIG = new ConfigManager(this);

        Objects.requireNonNull(getCommand("messagegroup")).setExecutor(new MessageGroupCommand());

        // register voice chat plugins
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(new SimpleVoiceChatAPI());
        } else {
            throw new RuntimeException("Simple Voice Chat BukkitVoicechatService was null! Is Simple Voice Chat installed?");
        }

        getLogger().info("Loaded successfully!");
    }
}
