package io.github.darkkronicle.advancedchathud;

import fi.dy.masa.malilib.event.InitializationHandler;
import io.github.darkkronicle.advancedchathud.tabs.MainChatTab;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AdvancedChatHud implements ClientModInitializer {

    public static final String MOD_ID = "advancedchathud";
    public static MainChatTab MAIN_CHAT_TAB;

    @Override
    public void onInitializeClient() {
        // This will run after AdvancedChatCore's because of load order
        InitializationHandler
            .getInstance()
            .registerInitializationHandler(new HudInitHandler());
    }
}
