/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.SaveableConfig;
import io.github.darkkronicle.advancedchatcore.gui.buttons.BackButtonListener;
import io.github.darkkronicle.advancedchatcore.gui.buttons.Buttons;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import io.github.darkkronicle.advancedchatcore.interfaces.IClosable;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;

public class GuiTabEditor extends GuiConfigsBase implements IClosable {

    private final ChatTab tab;
    private final boolean main;

    public GuiTabEditor(Screen parent, ChatTab tab) {
        this(parent, tab, false);
    }

    public GuiTabEditor(Screen parent, ChatTab tab, boolean main) {
        super(10, 50, AdvancedChatHud.MOD_ID, parent, tab.getName().config.getStringValue());
        this.tab = tab;
        this.main = main;
        this.setParent(parent);
    }

    @Override
    public void initGui() {
        super.initGui();
        createButtons(10, 26);
    }

    private void createButtons(int x, int y) {
        ButtonGeneric back = Buttons.BACK.createButton(x, y);
        x += this.addButton(back, new BackButtonListener(this)).getWidth() + 2;

        if (!main) {
            x += this.addButton(x, y, "advancedchathud.gui.button.matches", (button, mouseButton) -> {
                save();
                GuiBase.openGui(new MatchesEditor(this, tab));
            }) + 2;
        }

        this.addButton(
                x,
                y,
                "advancedchathud.gui.button.export",
                (button, mouseButton) -> {
                    save();
                    GuiBase.openGui(SharingScreen.fromTab(tab, this));
                });
    }

    private int addButton(int x, int y, String translation, IButtonActionListener listener) {
        return addButton(new NamedSimpleButton(x, y, StringUtils.translate(translation)), listener)
                .getWidth();
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        ArrayList<IConfigBase> config = new ArrayList<>();
        List<SaveableConfig<? extends IConfigBase>> options;
        if (main) {
            options = tab.getMainEditableOptions();
        } else {
            options = tab.getOptions();
        }
        for (SaveableConfig<? extends IConfigBase> s : options) {
            config.add(s.config);
        }

        return GuiConfigsBase.ConfigOptionWrapper.createFor(config);
    }

    @Override
    public void onClose() {
        save();
        super.onClose();
    }

    @Override
    protected void closeGui(boolean showParent) {
        // Save the changes :)
        save();
        super.closeGui(showParent);
    }

    public void save() {
        AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        // Override so that on escape stuff still gets saved
        if (this.activeKeybindButton != null) {
            this.activeKeybindButton.onKeyPressed(keyCode);
            return true;
        } else {
            if (this.getListWidget().onKeyTyped(keyCode, scanCode, modifiers)) {
                return true;
            }

            if (keyCode == KeyCodes.KEY_ESCAPE
                    && this.parentScreen != GuiUtils.getCurrentScreen()) {
                // Make sure to save
                closeGui(true);
                return true;
            }

            return false;
        }
    }

    @Override
    public void close(ButtonBase button) {
        closeGui(true);
    }
}
