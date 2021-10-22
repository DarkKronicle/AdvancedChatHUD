package io.github.darkkronicle.advancedchathud;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchatcore.chat.ChatHistory;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HudInitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(AdvancedChatHud.MOD_ID, new HudConfigStorage());
        GuiConfigHandler.getInstance().addGuiSection(GuiConfigHandler.createGuiConfigSection(
                StringUtils.translate("advancedchathud.tab.general"), HudConfigStorage.General.OPTIONS
        ));

        // Register on new message
        ChatHistory.getInstance().addOnMessage(chatMessage -> System.out.println("Chat message happened!"));
        // Register on the clear
        ChatHistory.getInstance().addOnClear(() -> System.out.println(HudConfigStorage.General.STRING_STUFF.config.getStringValue()));
    }

}
