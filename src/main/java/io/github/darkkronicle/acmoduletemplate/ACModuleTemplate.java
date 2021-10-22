package io.github.darkkronicle.acmoduletemplate;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ACModuleTemplate implements ClientModInitializer {

    public static final String MOD_ID = "acmoduletemplate";

    @Override
    public void onInitializeClient() {
        // This will run after AdvancedChatCore's because of load order
        InitializationHandler.getInstance().registerInitializationHandler(new ModuleInitHandler());
    }

}
