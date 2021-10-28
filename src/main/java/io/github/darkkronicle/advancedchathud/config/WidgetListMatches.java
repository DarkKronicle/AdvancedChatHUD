/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;

public class WidgetListMatches extends WidgetListBase<Match, WidgetMatchEntry> {

    public final ChatTab tab;
    protected final List<TextFieldWrapper<GuiTextFieldGeneric>> textFields = new ArrayList<>();

    @Override
    protected void reCreateListEntryWidgets() {
        textFields.clear();
        super.reCreateListEntryWidgets();
    }

    public void addTextField(TextFieldWrapper<GuiTextFieldGeneric> text) {
        textFields.add(text);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        clearTextFieldFocus();
        return super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void clearTextFieldFocus() {
        for (TextFieldWrapper<GuiTextFieldGeneric> field : this.textFields) {
            GuiTextFieldGeneric textField = field.getTextField();

            if (textField.isFocused()) {
                textField.setFocused(false);
                break;
            }
        }
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        for (WidgetMatchEntry widget : this.listWidgets) {
            if (widget.onKeyTyped(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.onKeyTyped(keyCode, scanCode, modifiers);
    }

    public WidgetListMatches(
            int x,
            int y,
            int width,
            int height,
            ISelectionListener<Match> selectionListener,
            ChatTab parent,
            Screen screen) {
        super(x, y, width, height, selectionListener);
        this.browserEntryHeight = 22;
        this.tab = parent;
        this.setParent(screen);
    }

    @Override
    protected WidgetMatchEntry createListEntryWidget(
            int x, int y, int listIndex, boolean isOdd, Match entry) {
        return new WidgetMatchEntry(
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
    protected Collection<Match> getAllEntries() {
        return tab.getMatches();
    }
}
