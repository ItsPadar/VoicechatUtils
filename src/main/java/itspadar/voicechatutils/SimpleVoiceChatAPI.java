package itspadar.voicechatutils;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

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

}