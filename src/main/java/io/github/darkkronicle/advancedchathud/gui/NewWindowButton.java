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
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class NewWindowButton extends CleanButton {

    private static final int PADDING = 3;
    private static final int SIZE = PADDING + 9 + PADDING;
    private static final Identifier ADD_ICON =
            new Identifier(AdvancedChatHud.MOD_ID, "textures/gui/chatwindow/add_window.png");

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

        RenderUtils.color(1, 1, 1, 1);
        RenderUtils.bindTexture(ADD_ICON);
        DrawableHelper.drawTexture(
                matrixStack,
                x + PADDING,
                y + PADDING,
                width - (PADDING * 2),
                height - (PADDING * 2),
                0,
                0,
                32,
                32,
                32,
                32);
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
