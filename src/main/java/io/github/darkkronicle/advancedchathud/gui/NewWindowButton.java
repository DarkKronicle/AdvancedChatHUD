/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.gui;

import fi.dy.masa.malilib.render.RenderUtils;
import io.github.darkkronicle.advancedchatcore.gui.CleanButton;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class NewWindowButton extends CleanButton {

    private static final int PADDING = 3;
    private static final int SIZE = PADDING + 9 + PADDING;

    public NewWindowButton(int x, int y) {
        super(x, y, SIZE, SIZE, null, null);
    }

    @Override
    public void render(int mouseX, int mouseY, boolean unused, MatrixStack matrixStack) {
        int relMX = mouseX - x;
        int relMY = mouseY - y;
        hovered = relMX >= 0 && relMX <= width && relMY >= 0 && relMY <= height;

        ColorUtil.SimpleColor plusBack = ColorUtil.BLACK.withAlpha(100);
        boolean plusHovered = hovered && relMX >= width - height;
        if (plusHovered) {
            plusBack = ColorUtil.WHITE.withAlpha(plusBack.alpha());
        }

        RenderUtils.drawRect(x, y, width, height, plusBack.color());

        RenderUtils.drawVerticalLine(
                x + (int) Math.floor((float) width / 2),
                y + PADDING,
                height - (PADDING * 2),
                ColorUtil.WHITE.color());
        RenderUtils.drawHorizontalLine(
                x + PADDING,
                y + (int) Math.floor((float) height / 2),
                width - (PADDING * 2),
                ColorUtil.WHITE.color());
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        this.mc
                .getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        WindowManager.getInstance().onTabAddButton(IChatHud.getInstance().getTab());
        return true;
    }
}
