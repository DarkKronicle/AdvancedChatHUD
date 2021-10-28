/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import java.util.List;
import javax.annotation.Nullable;

public class GuiTabManager extends GuiListBase<ChatTab, WidgetTabEntry, WidgetListTabs>
        implements ISelectionListener<ChatTab> {

    private List<GuiConfigHandler.TabButton> tabButtons;

    public GuiTabManager(List<GuiConfigHandler.TabButton> tabButtons) {
        super(10, 60);
        this.title = StringUtils.translate("advancedchat.screen.main");
        this.tabButtons = tabButtons;
    }

    @Override
    protected WidgetListTabs createListWidget(int listX, int listY) {
        return new WidgetListTabs(
                listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), null, this);
    }

    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    public void initGui() {
        super.initGui();

        int x = 10;
        int y = 26;

        int rows = 1;

        for (GuiConfigHandler.TabButton tab : tabButtons) {
            int newY = this.createButton(tab, y);
            if (newY != y) {
                rows++;
                y = newY;
            }
        }

        this.setListPosition(this.getListX(), 68 + (rows - 1) * 22);
        this.reCreateListWidget();
        this.getListWidget().refreshEntries();

        y += 24;
        x = this.width - 10;
        x -= this.addButton(x, y, ButtonListener.Type.ADD_TAB) + 2;
        x -= this.addButton(x, y, ButtonListener.Type.IMPORT) + 2;
    }

    private int createButton(GuiConfigHandler.TabButton button, int y) {
        this.addButton(button.getButton(), new ButtonListenerConfigTabs(button));
        return button.getButton().getY();
    }

    protected int addButton(int x, int y, ButtonListener.Type type) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, true, type.getDisplayName());
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth();
    }

    private static class ButtonListener implements IButtonActionListener {

        private final ButtonListener.Type type;
        private final GuiTabManager gui;

        public ButtonListener(ButtonListener.Type type, GuiTabManager gui) {
            this.type = type;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (this.type == ButtonListener.Type.ADD_TAB) {
                HudConfigStorage.TABS.add(new ChatTab());
                this.gui.getListWidget().refreshEntries();
            } else if (this.type == Type.IMPORT) {
                GuiBase.openGui(new SharingScreen(null, gui));
            }
        }

        public enum Type {
            ADD_TAB("addtab"),
            IMPORT("import");

            private static String translate(String key) {
                return "advancedchathud.gui.button." + key;
            }

            private final String translationKey;

            Type(String translationKey) {
                this.translationKey = translate(translationKey);
            }

            public String getDisplayName() {
                return StringUtils.translate(this.translationKey);
            }
        }
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - 6 - this.getListY();
    }

    @Override
    public void onSelectionChange(@Nullable ChatTab entry) {}

    private static class ButtonListenerConfigTabs implements IButtonActionListener {

        private final GuiConfigHandler.TabButton tabButton;

        public ButtonListenerConfigTabs(GuiConfigHandler.TabButton tabButton) {
            this.tabButton = tabButton;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            GuiConfigHandler.getInstance().activeTab = this.tabButton.getTab().getName();
            GuiBase.openGui(
                    this.tabButton.getTab().getScreen(GuiConfigHandler.getInstance().getButtons()));
        }
    }
}
