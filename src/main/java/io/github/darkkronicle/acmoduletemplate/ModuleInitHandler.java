package io.github.darkkronicle.acmoduletemplate;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.acmoduletemplate.config.ModuleConfigStorage;
import io.github.darkkronicle.advancedchatcore.chat.ChatHistory;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModuleInitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(ACModuleTemplate.MOD_ID, new ModuleConfigStorage());
        GuiConfigHandler.getInstance().addGuiSection(GuiConfigHandler.createGuiConfigSection(
                StringUtils.translate("acmoduletemplate.tab.general"), ModuleConfigStorage.General.OPTIONS
        ));

        // Register on new message
        ChatHistory.getInstance().addOnMessage(chatMessage -> System.out.println("Chat message happened!"));
        // Register on the clear
        ChatHistory.getInstance().addOnClear(() -> System.out.println(ModuleConfigStorage.General.STRING_STUFF.config.getStringValue()));
    }

}
