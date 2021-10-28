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
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class TabButton extends CleanButton {

    private final AbstractChatTab tab;

    private TabButton(AbstractChatTab tab, int x, int y, int width, int height) {
        super(x, y, width, height, tab.getMainColor(), tab.getAbreviation());
        this.tab = tab;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {
        int relMX = mouseX - x;
        int relMY = mouseY - y;
        hovered = relMX >= 0 && relMX <= width && relMY >= 0 && relMY <= height;
        ColorUtil.SimpleColor color = baseColor;
        if (hovered) {
            color = ColorUtil.WHITE.withAlpha(color.alpha());
        }
        ColorUtil.SimpleColor plusBack = ColorUtil.BLACK.withAlpha(100);
        boolean plusHovered = hovered && relMX >= width - height;
        if (plusHovered) {
            plusBack = ColorUtil.WHITE.withAlpha(plusBack.alpha());
        }
        RenderUtils.drawRect(x, y, width - height, height, color.color());
        RenderUtils.drawOutline(
                x, y, width - height + 1, height, ColorUtil.BLACK.withAlpha(180).color());
        RenderUtils.drawRect(x + width - height, y, height, height, plusBack.color());
        ColorUtil.SimpleColor crossColor = ColorUtil.WHITE.withAlpha(plusBack.alpha());
        RenderUtils.drawVerticalLine(
                x + width - (int) Math.floor((float) height / 2) - 1,
                y + 1,
                height - 2,
                crossColor.color());
        RenderUtils.drawHorizontalLine(
                x + width - height + 1,
                y + (int) Math.floor((float) height / 2),
                height - 2,
                crossColor.color());
        RenderUtils.drawOutline(
                x + width - height, y, height, height, ColorUtil.BLACK.withAlpha(180).color());

        int center = tab.isShowUnread() ? ((width - 20 - height) / 2) : ((width - height) / 2);
        drawCenteredString(
                x + center,
                (y + (height / 2) - 3),
                ColorUtil.WHITE.color(),
                displayString,
                matrixStack);
        if (tab.isShowUnread() && tab.getUnread() > 0) {
            String unread;
            if (tab.getUnread() <= 99) {
                unread = tab.getUnread() + "";
            } else {
                unread = "99+";
            }
            drawString(
                    x + width - height - 16,
                    (y + (height / 2) - 3),
                    ColorUtil.WHITE.color(),
                    unread,
                    matrixStack);
        }
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        this.mc
                .getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        if (mouseX - x >= width - height) {
            // Plus button
            WindowManager.getInstance().onTabAddButton(tab);
        } else {
            WindowManager.getInstance().onTabButton(tab);
        }
        return true;
    }

    public static TabButton fromTab(AbstractChatTab tab, int x, int y) {
        int width = StringUtils.getStringWidth(tab.getAbreviation()) + 19;
        // TODO here something
        if (tab.isShowUnread()) {
            width += 16;
        }
        return new TabButton(tab, x, y, width, 11);
    }
}
