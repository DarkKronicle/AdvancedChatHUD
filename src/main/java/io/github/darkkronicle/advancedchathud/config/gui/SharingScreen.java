/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import net.minecraft.client.gui.screen.Screen;

/** Screen for importing and exporting {@link ChatTab}. */
public class SharingScreen extends GuiBase {

    private final String starting;
    private static final Gson GSON = new GsonBuilder().create();
    private GuiTextFieldGeneric text;

    public SharingScreen(String starting, Screen parent) {
        this.setParent(parent);
        this.setTitle(StringUtils.translate("advancedchat.gui.menu.import"));
        this.starting = starting;
    }

    /** Creates a SharingScreen from a tab */
    public static SharingScreen fromTab(ChatTab tab, Screen parent) {
        ChatTab.ChatTabJsonSave tabJsonSave = new ChatTab.ChatTabJsonSave();
        return new SharingScreen(GSON.toJson(tabJsonSave.save(tab)), parent);
    }

    @Override
    public void init() {
        int x = this.width / 2 - 150;
        int y = 50;
        text = new GuiTextFieldGeneric(x, y, 300, 20, client.textRenderer);
        y -= 24;
        text.setMaxLength(12800);
        if (starting != null) {
            text.setText(starting);
            text.setTextFieldFocused(true);
        }

        text.changeFocus(true);
        text.setDrawsBackground(true);
        text.setEditable(true);
        text.changeFocus(true);
        this.addTextField(text, null);

        this.addButton(
                x, y, "advancedchat.gui.button.importtab", (button, mouseButton) -> importTab());
    }

    private void addButton(int x, int y, String translation, IButtonActionListener listener) {
        this.addButton(new NamedSimpleButton(x, y, StringUtils.translate(translation)), listener);
    }

    public void importTab() {
        ChatTab.ChatTabJsonSave tabSave = new ChatTab.ChatTabJsonSave();
        ChatTab tab = tabSave.load(new JsonParser().parse(text.getText()).getAsJsonObject());
        if (tab == null) {
            throw new NullPointerException("Filter is null!");
        }
        HudConfigStorage.TABS.add(tab);
        AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
        addGuiMessage(
                Message.MessageType.SUCCESS,
                5000,
                StringUtils.translate("advancedchat.gui.message.successful"));
    }
}
