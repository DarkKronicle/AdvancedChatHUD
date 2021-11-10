/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config.gui;

import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import io.github.darkkronicle.advancedchatcore.gui.WidgetConfigList;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.client.gui.screen.Screen;

public class WidgetListTabs extends WidgetConfigList<ChatTab, WidgetTabEntry> {

    public WidgetListTabs(
            int x,
            int y,
            int width,
            int height,
            ISelectionListener<ChatTab> selectionListener,
            Screen parent) {
        super(x, y, width, height, selectionListener, parent);
        this.setParent(parent);
    }

    @Override
    protected WidgetTabEntry createListEntryWidget(
            int x, int y, int listIndex, boolean isOdd, ChatTab entry) {
        return new WidgetTabEntry(
                x,
                y,
                this.browserEntryWidth,
                this.getBrowserEntryHeightFor(entry),
                isOdd,
                entry,
                listIndex,
                this);
    }

    @Override
    protected Collection<ChatTab> getAllEntries() {
        ArrayList<ChatTab> tabs = new ArrayList<>(Collections.singleton(HudConfigStorage.MAIN_TAB));
        tabs.addAll(HudConfigStorage.TABS);
        return tabs;
    }
}
