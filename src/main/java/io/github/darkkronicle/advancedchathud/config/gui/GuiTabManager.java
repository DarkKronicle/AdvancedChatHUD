/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import io.github.darkkronicle.advancedchatcore.gui.ConfigGuiListBase;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import java.util.List;

public class GuiTabManager extends ConfigGuiListBase<ChatTab, WidgetTabEntry, WidgetListTabs> {

    public GuiTabManager() {
        super();
        this.title = StringUtils.translate("advancedchat.screen.main");
    }

    @Override
    protected WidgetListTabs createListWidget(int listX, int listY) {
        return new WidgetListTabs(
                listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), null, this);
    }

    @Override
    public void initGuiConfig(int x, int y) {
        x = this.width - 10;
        y -= 48;
        x -= this.addButton(x, y, "advancedchathud.gui.button.addtab", (button, mouseButton) -> {
            HudConfigStorage.TABS.add(new ChatTab());
            this.getListWidget().refreshEntries();
        }) + 2;
        x -= this.addButton(x, y, "advancedchathud.gui.button.import", (button, mouseButton) ->
                GuiBase.openGui(new SharingScreen(null, this))
        ) + 2;
        x -= this.addButton(x, y, "advancedchathud.gui.button.reload", (button, mouseButton) ->
                AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs()
        ) + 2;
    }

    protected int addButton(int x, int y, String translation, IButtonActionListener listener) {
        ButtonGeneric button = new NamedSimpleButton(x, y, StringUtils.translate(translation), false);
        this.addButton(button, listener);
        return button.getWidth();
    }
}
