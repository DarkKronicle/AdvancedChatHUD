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
import io.github.darkkronicle.advancedchatcore.gui.WidgetConfigListEntry;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WidgetTabEntry extends WidgetConfigListEntry<ChatTab> {

    private final ChatTab tab;
    private boolean main = true;

    public WidgetTabEntry(
            int x,
            int y,
            int width,
            int height,
            boolean isOdd,
            ChatTab tab,
            int listIndex,
            WidgetListTabs parent) {
        super(x, y, width, height, isOdd, tab, listIndex, tab.getWidgetHoverLines());
        this.tab = tab;
        y += 1;
        int pos = x + width - 2;
        if (listIndex != 0) {
            main = false;
            // If it's 0 it's the main tab
            pos -=
                    addButton(
                            pos,
                            y,
                            "advancedchathud.config.tabmenu.remove",
                            (button, mouseButton) -> {
                                HudConfigStorage.TABS.remove(tab);
                                parent.refreshEntries();
                                AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
                            });
        }
        pos -=
                addButton(
                        pos,
                        y,
                        "advancedchathud.config.tabmenu.configure",
                        (button, mouseButton) -> {
                            GuiBase.openGui(new GuiTabEditor(parent.getParent(), tab, main));
                        });
        setButtonStartX(pos);
    }

    protected int addButton(int x, int y, String translation, IButtonActionListener listener) {
        ButtonGeneric button =
                new NamedSimpleButton(x, y, StringUtils.translate(translation), false);
        this.addButton(button, listener);
        return button.getWidth();
    }

    @Override
    public String getName() {
        return tab.getName().config.getStringValue();
    }
}
