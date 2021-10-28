/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import net.minecraft.client.gui.screen.Screen;

public class MatchesEditor extends GuiListBase<Match, WidgetMatchEntry, WidgetListMatches> {

    private final ChatTab tab;

    @Override
    public void initGui() {
        super.initGui();
        this.setListPosition(this.getListX(), 68);
        int y = 26;
        int x = this.width - 10;
        this.addButton(x, y, ButtonListener.Type.ADD_MATCH, true);
        this.addButton(2, y, ButtonListener.Type.BACK, false);
    }

    @Override
    protected void closeGui(boolean showParent) {
        AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
        super.closeGui(showParent);
    }

    protected MatchesEditor(Screen parent, ChatTab tab) {
        super(10, 60);
        this.title = StringUtils.translate("advancedchathud.config.match.screen.name");
        this.tab = tab;
        this.setParent(parent);
    }

    protected int addButton(int x, int y, ButtonListener.Type type, boolean rightAlign) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, rightAlign, type.getDisplayName());
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth();
    }

    @Override
    protected WidgetListMatches createListWidget(int listX, int listY) {
        return new WidgetListMatches(
                listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), null, tab, this);
    }

    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - 6 - this.getListY();
    }

    public void addMatch() {
        tab.getMatches().add(new Match("I will match to text!", FindType.LITERAL));
        getListWidget().refreshEntries();
    }

    public void back() {
        this.closeGui(true);
    }

    private static class ButtonListener implements IButtonActionListener {

        private final ButtonListener.Type type;
        private final MatchesEditor gui;

        public ButtonListener(ButtonListener.Type type, MatchesEditor gui) {
            this.type = type;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (this.type == ButtonListener.Type.ADD_MATCH) {
                this.gui.addMatch();
            } else if (this.type == ButtonListener.Type.BACK) {
                this.gui.back();
            }
        }

        public enum Type {
            ADD_MATCH("advancedchathud.gui.button.addmatch"),
            BACK("advancedchathud.button.back");

            private final String translationKey;

            Type(String translationKey) {
                this.translationKey = translationKey;
            }

            public String getDisplayName() {
                return StringUtils.translate(this.translationKey);
            }
        }
    }
}
