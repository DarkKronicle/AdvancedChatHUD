/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class WidgetMatchEntry extends WidgetListEntryBase<Match> {

    private final WidgetListMatches parent;
    private final boolean isOdd;
    private TextFieldWrapper<GuiTextFieldGeneric> name;
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
        super(x, y, width, height, entry, listIndex);
        this.isOdd = isOdd;
        this.parent = parent;
        y += 1;
        int pos = x + width - 2;

        int removeWidth = 0;
        if (listIndex != 0) {
            // Always want one
            removeWidth = addButton(pos, y, ButtonListener.Type.REMOVE) + 1;
        }
        pos -= removeWidth;
        int replaceWidth = 100;
        int nameWidth = width - replaceWidth - removeWidth;
        this.findType.setOptionListValue(entry.getFindType());
        ConfigButtonOptionList findType =
                new ConfigButtonOptionList(pos - replaceWidth, y, replaceWidth, 20, this.findType);
        this.addButton(
                findType,
                (button, mouseButton) -> {
                    entry.setFindType((FindType) this.findType.getOptionListValue());
                });

        pos -= replaceWidth + 1;
        GuiTextFieldGeneric nameField =
                new GuiTextFieldGeneric(
                        pos - nameWidth,
                        y,
                        nameWidth,
                        20,
                        MinecraftClient.getInstance().textRenderer);
        nameField.setMaxLength(64000);
        nameField.setText(entry.getPattern());
        name = new TextFieldWrapper<>(nameField, new SaveListener(this));
        parent.addTextField(name);
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

    protected int addButton(int x, int y, ButtonListener.Type type) {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, true, type.getDisplayName());
        this.addButton(button, new ButtonListener(type, this));

        return button.getWidth() + 1;
    }

    @Override
    protected boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers) {
        if (this.name.isFocused()) {
            return this.name.onKeyTyped(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    protected boolean onCharTypedImpl(char charIn, int modifiers) {
        if (this.name.onCharTyped(charIn, modifiers)) {
            return true;
        }

        return super.onCharTypedImpl(charIn, modifiers);
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        if (super.onMouseClickedImpl(mouseX, mouseY, mouseButton)) {
            return true;
        }

        boolean ret = false;

        if (this.name != null) {
            ret = this.name.getTextField().mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (!this.subWidgets.isEmpty()) {
            for (WidgetBase widget : this.subWidgets) {
                ret |=
                        widget.isMouseOver(mouseX, mouseY)
                                && widget.onMouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        return ret;
    }

    protected void drawTextFields(int mouseX, int mouseY, MatrixStack matrixStack) {
        if (this.name != null) {
            this.name.getTextField().render(matrixStack, mouseX, mouseY, 0f);
        }
    }

    private static class ButtonListener implements IButtonActionListener {

        private final ButtonListener.Type type;
        private final WidgetMatchEntry parent;

        public ButtonListener(ButtonListener.Type type, WidgetMatchEntry parent) {
            this.parent = parent;
            this.type = type;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (type == ButtonListener.Type.REMOVE) {
                parent.parent.tab.getMatches().remove(parent.entry);
                parent.parent.refreshEntries();
            }
        }

        public enum Type {
            REMOVE("remove");

            private final String translate;

            Type(String name) {
                this.translate = translate(name);
            }

            private static String translate(String key) {
                return "advancedchathud.config.match." + key;
            }

            public String getDisplayName() {
                return StringUtils.translate(translate);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {
        RenderUtils.color(1f, 1f, 1f, 1f);

        // Draw a lighter background for the hovered and the selected entry
        if (selected || this.isMouseOver(mouseX, mouseY)) {
            RenderUtils.drawRect(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    ColorUtil.WHITE.withAlpha(150).color());
        } else if (this.isOdd) {
            RenderUtils.drawRect(
                    this.x, this.y, this.width, this.height, ColorUtil.WHITE.withAlpha(70).color());
        } else {
            RenderUtils.drawRect(
                    this.x, this.y, this.width, this.height, ColorUtil.WHITE.withAlpha(50).color());
        }

        RenderUtils.color(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        this.drawTextFields(mouseX, mouseY, matrixStack);

        super.render(mouseX, mouseY, selected, matrixStack);

        RenderUtils.disableDiffuseLighting();
    }
}
