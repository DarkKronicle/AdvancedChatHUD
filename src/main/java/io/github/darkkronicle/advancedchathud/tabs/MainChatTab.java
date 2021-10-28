/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.tabs;

import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessageHolder;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import java.util.ArrayList;
import java.util.UUID;
import lombok.Getter;
import net.minecraft.text.Text;

/** Main chat tab that manages other chat tabs. */
public class MainChatTab extends AbstractChatTab {

    @Getter private ArrayList<AbstractChatTab> allChatTabs = new ArrayList<>();

    @Getter private ArrayList<CustomChatTab> customChatTabs = new ArrayList<>();

    public MainChatTab() {
        super(
                "Main",
                HudConfigStorage.MAIN_TAB.getAbbreviation().config.getStringValue(),
                HudConfigStorage.MAIN_TAB.getMainColor().config.getSimpleColor(),
                HudConfigStorage.MAIN_TAB.getBorderColor().config.getSimpleColor(),
                HudConfigStorage.MAIN_TAB.getInnerColor().config.getSimpleColor(),
                HudConfigStorage.MAIN_TAB.getShowUnread().config.getBooleanValue(),
                HudConfigStorage.MAIN_TAB.getUuid());
        setUpTabs();
    }

    public void refreshOptions() {
        this.abreviation = HudConfigStorage.MAIN_TAB.getAbbreviation().config.getStringValue();
        this.mainColor = HudConfigStorage.MAIN_TAB.getMainColor().config.getSimpleColor();
        this.innerColor = HudConfigStorage.MAIN_TAB.getInnerColor().config.getSimpleColor();
        this.borderColor = HudConfigStorage.MAIN_TAB.getBorderColor().config.getSimpleColor();
        this.showUnread = HudConfigStorage.MAIN_TAB.getShowUnread().config.getBooleanValue();
        this.uuid = HudConfigStorage.MAIN_TAB.getUuid();
    }

    @Override
    public boolean shouldAdd(Text text) {
        return true;
    }

    /** Method used for loading in tabs from the config. */
    public void setUpTabs() {
        customChatTabs = new ArrayList<>();
        allChatTabs = new ArrayList<>();
        allChatTabs.add(this);
        for (ChatTab tab : HudConfigStorage.TABS) {
            CustomChatTab customTab = new CustomChatTab(tab);
            customChatTabs.add(customTab);
            allChatTabs.add(customTab);
        }
        for (HudChatMessage message : HudChatMessageHolder.getInstance().getMessages()) {
            ArrayList<AbstractChatTab> tabs = new ArrayList<>();
            for (AbstractChatTab t : allChatTabs) {
                if (t.shouldAdd(message.getMessage().getOriginalText())) {
                    tabs.add(t);
                }
            }
            message.setTabs(tabs);
        }
        this.refreshOptions();
    }

    public AbstractChatTab fromUUID(UUID uuid) {
        for (AbstractChatTab tab : allChatTabs) {
            if (tab.getUuid().equals(uuid)) {
                return tab;
            }
        }
        return null;
    }
}
