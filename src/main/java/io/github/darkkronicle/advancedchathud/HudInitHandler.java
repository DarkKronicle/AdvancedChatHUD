/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import io.github.darkkronicle.advancedchatcore.chat.ChatHistory;
import io.github.darkkronicle.advancedchatcore.chat.ChatScreenSectionHolder;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.config.gui.GuiTabManager;
import io.github.darkkronicle.advancedchathud.gui.HudSection;
import io.github.darkkronicle.advancedchathud.gui.WindowManager;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import io.github.darkkronicle.advancedchathud.tabs.MainChatTab;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public class HudInitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        AdvancedChatCore.FORWARD_TO_HUD = false;
        ConfigManager.getInstance()
                .registerConfigHandler(AdvancedChatHud.MOD_ID, new HudConfigStorage());
        GuiConfigHandler.getInstance()
                .addGuiSection(
                        GuiConfigHandler.createGuiConfigSection(
                                StringUtils.translate("advancedchathud.tab.general"),
                                HudConfigStorage.General.OPTIONS));
        GuiConfigHandler.getInstance()
                .addGuiSection(
                        new GuiConfigHandler.Tab() {
                            @Override
                            public String getName() {
                                return StringUtils.translate("advancedchathud.tab.tabs");
                            }

                            @Override
                            public Screen getScreen(List<GuiConfigHandler.TabButton> buttons) {
                                return new GuiTabManager(buttons);
                            }
                        });
        IChatHud.getInstance().setTab(AdvancedChatHud.MAIN_CHAT_TAB = new MainChatTab());

        // Register on the clear
        ChatScreenSectionHolder.getInstance().addSectionSupplier(HudSection::new);
        ChatHistory.getInstance().addOnClear(() -> WindowManager.getInstance().clear());
        ChatHistory.getInstance().addOnClear(() -> HudChatMessageHolder.getInstance().clear());
        ChatHistory.getInstance().addOnUpdate(HudChatMessageHolder.getInstance());
        RenderEventHandler.getInstance().registerGameOverlayRenderer(WindowManager.getInstance());
        ResolutionEventHandler.ON_RESOLUTION_CHANGE.add(WindowManager.getInstance());
    }
}
