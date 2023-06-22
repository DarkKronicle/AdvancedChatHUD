/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config.gui;

import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.gui.WidgetConfigListEntry;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.TextUtil;
import io.github.darkkronicle.advancedchathud.config.Match;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class WidgetMatchEntry extends WidgetConfigListEntry<Match> {

    private TextFieldWrapper<GuiTextFieldGeneric> name;
    private List<TextFieldWrapper<GuiTextFieldGeneric>> texts;
    private ConfigOptionList findType =
            new ConfigOptionList(
                    "advancedchathud.config.match.findtype",
                    FindType.LITERAL,
                    "advancedchathud.config.match.info.findtype");

    public WidgetMatchEntry(
            int x,
            int y,
            int width,
            int height,
            boolean isOdd,
            Match entry,
            int listIndex,
            WidgetListMatches parent) {
        super(x, y, width, height, isOdd, entry, listIndex);
        y += 1;
        int pos = x + width - 2;

        int removeWidth = 0;
        if (listIndex != 0) {
            // Always want one
            removeWidth = addButton(pos, y, "advancedchathud.config.match.remove", (button, mouseButton) -> {
                parent.tab.getMatches().remove(entry);
                parent.refreshEntries();
            }) + 1;
        }
        pos -= removeWidth;
        int findWidth = getFindTypeWidth();
        int nameWidth = width - findWidth - removeWidth;
        this.findType.setOptionListValue(entry.getFindType());
        ConfigButtonOptionList findType =
                new ConfigButtonOptionList(pos - findWidth, y, findWidth, 20, this.findType);
        this.addButton(
                findType,
                (button, mouseButton) -> {
                    entry.setFindType((FindType) this.findType.getOptionListValue());
                });

        pos -= findWidth + 1;
        GuiTextFieldGeneric nameField = new GuiTextFieldGeneric(pos - nameWidth, y, nameWidth, 20, MinecraftClient.getInstance().textRenderer);
        nameField.setMaxLength(64000);
        nameField.setText(entry.getPattern());
        name = new TextFieldWrapper<>(nameField, new SaveListener(this));
        parent.addTextField(name);
        texts = new ArrayList<>();
        texts.add(name);
    }

    private static int getFindTypeWidth() {
        List<String> translations = new ArrayList<>();
        for (FindType f : FindType.values()) {
            translations.add(f.getDisplayName());
        }
        return TextUtil.getMaxLengthTranslation(translations) + 10;
    }

    @Override
    public List<TextFieldWrapper<GuiTextFieldGeneric>> getTextFields() {
        return texts;
    }

    @Override
    public void renderEntry(int mouseX, int mouseY, boolean selected, DrawContext context) {}

    @Override
    public String getName() {
        return null;
    }

    public void save() {
        entry.setPattern(name.getTextField().getText());
    }

    private static class SaveListener implements ITextFieldListener<GuiTextFieldGeneric> {

        private final WidgetMatchEntry parent;

        public SaveListener(WidgetMatchEntry parent) {
            this.parent = parent;
        }

        @Override
        public boolean onTextChange(GuiTextFieldGeneric textField) {
            parent.entry.setPattern(textField.getText());
            return false;
        }
    }

    protected int addButton(int x, int y, String translation, IButtonActionListener listener) {
        ButtonGeneric button =
                new NamedSimpleButton(x, y, StringUtils.translate(translation), false);
        this.addButton(button, listener);
        return button.getWidth();
    }
}
