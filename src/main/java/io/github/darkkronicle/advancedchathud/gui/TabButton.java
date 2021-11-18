/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.gui;

import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.gui.CleanButton;
import io.github.darkkronicle.advancedchatcore.util.Color;
import io.github.darkkronicle.advancedchatcore.util.Colors;
import io.github.darkkronicle.advancedchatcore.util.TextUtil;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class TabButton extends CleanButton {

    private final AbstractChatTab tab;
    private static final int PADDING = 3;
    private static final int UNREAD_WIDTH = 9; // reserve 9px for unread

    private static final int WHITE = 0xFF_FF_FF_FF;
    private static final int GRAY = 0xFF_AA_AA_AA;
    private static final int RED = 0xFF_FF_55_55;

    private TabButton(AbstractChatTab tab, int x, int y, int width, int height) {
        super(x, y, width, height, tab.getMainColor(), tab.getAbreviation());
        this.tab = tab;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean unused, MatrixStack matrixStack) {
        int relMX = mouseX - x;
        int relMY = mouseY - y;
        hovered = relMX >= 0 && relMX <= width && relMY >= 0 && relMY <= height;
        Color color = baseColor;
        if (hovered) {
            color = Colors.getInstance().getColorOrWhite("hover").withAlpha(color.alpha());
        }

        boolean selected = false;
        if (HudConfigStorage.General.VANILLA_HUD.config.getBooleanValue()) {
            selected = IChatHud.getInstance().getTab().equals(tab);
        } else {
            ChatWindow window = WindowManager.getInstance().getSelected();
            if (window != null) {
                selected = window.getTab().equals(tab);
            }
        }
        if (!selected) {
            color = new Color(color.red() / 2, color.green() / 2, color.blue() / 2, 100);
        }

        RenderUtils.drawRect(x, y, width, height, color.color());

        drawStringWithShadow(
                x + PADDING, y + PADDING, selected ? WHITE : GRAY, displayString, matrixStack);
        if (tab.isShowUnread() && tab.getUnread() > 0) {
            String unread = TextUtil.toSuperscript(Math.min(tab.getUnread(), 99));
            drawCenteredString(
                    x + width - ((UNREAD_WIDTH + PADDING) / 2) - 1,
                    y + PADDING,
                    RED,
                    unread,
                    matrixStack);
        }
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        this.mc
                .getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        WindowManager.getInstance().onTabButton(tab);
        return true;
    }

    public static TabButton fromTab(AbstractChatTab tab, int x, int y) {
        int width = StringUtils.getStringWidth(tab.getAbreviation()) + PADDING * 2;
        if (tab.isShowUnread()) {
            width += UNREAD_WIDTH;
        }
        return new TabButton(tab, x, y, width, PADDING + 9 + PADDING);
    }
}
