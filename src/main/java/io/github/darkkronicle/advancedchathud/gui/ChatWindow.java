/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonSave;
import io.github.darkkronicle.advancedchatcore.util.Color;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchatcore.util.Colors;
import io.github.darkkronicle.advancedchatcore.util.EasingMethod;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.LimitedInteger;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessageHolder;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.darkkronicle.advancedchathud.util.ScissorUtil;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class ChatWindow {

    private double scrolledHeight = 0;
    private double scrollStart = 0;
    private double scrollEnd = 0;

    private long lastScroll = 0;
    private int scrollDuration = 200;

    @Getter @Setter private double yPercent;

    @Getter @Setter private double xPercent;

    @Getter @Setter private double widthPercent;

    @Getter @Setter private double heightPercent;

    @Getter @Setter private boolean renderRight = false;

    @Getter @Setter private boolean minimalist = false;

    @Getter @Setter private boolean renderTopFirst = false;

    private final MinecraftClient client;

    @Setter @Getter
    private HudConfigStorage.Visibility visibility =
            (HudConfigStorage.Visibility)
                    HudConfigStorage.General.VISIBILITY.config.getOptionListValue();

    private List<ChatMessage> lines;

    @Getter @Setter private boolean selected;

    @Getter private AbstractChatTab tab;

    private static final Identifier X_ICON =
            new Identifier(AdvancedChatHud.MOD_ID, "textures/gui/chatwindow/x_icon.png");

    private static final Identifier RESIZE_ICON =
            new Identifier(AdvancedChatHud.MOD_ID, "textures/gui/chatwindow/resize_icon.png");

    public ChatWindow(AbstractChatTab tab) {
        this.client = MinecraftClient.getInstance();
        int scaledHeight = client.getWindow().getScaledHeight();
        int scaledWidth = client.getWindow().getScaledWidth();
        this.yPercent =
                ((double) (scaledHeight - HudConfigStorage.General.Y.config.getIntegerValue()))
                        / scaledHeight;
        this.xPercent =
                ((double) HudConfigStorage.General.X.config.getIntegerValue()) / scaledWidth;
        this.widthPercent =
                ((double) HudConfigStorage.General.WIDTH.config.getIntegerValue()) / scaledWidth;
        this.heightPercent =
                ((double) HudConfigStorage.General.HEIGHT.config.getIntegerValue()) / scaledHeight;
        this.setTab(tab);
    }

    public void setRelativePosition(double x, double y) {
        if (x > 2) {
            x = 0;
        }
        if (y > 2) {
            y = 0;
        }
        this.xPercent = x;
        this.yPercent = y;
    }

    public void setPosition(int x, int y) {
        int scaledHeight = client.getWindow().getScaledHeight();
        this.xPercent = ((double) x) / client.getWindow().getScaledWidth();
        this.yPercent = ((double) y) / scaledHeight;
    }

    public void setRelativeDimensions(double width, double height) {
        this.widthPercent = width;
        this.heightPercent = height;
    }

    public void setTab(AbstractChatTab tab) {
        this.tab = tab;
        this.lines = new ArrayList<>();
        List<HudChatMessage> messages = HudChatMessageHolder.getInstance().getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            addMessage(messages.get(i), false, false);
        }
    }

    public void addMessage(HudChatMessage message) {
        this.addMessage(message, false, true);
    }

    public void addMessage(HudChatMessage message, boolean force, boolean setTicks) {
        if (force || message.getTabs().contains(tab)) {
            ChatMessage newMessage = message.getMessage().shallowClone(getPaddedWidth());
            if (setTicks) {
                newMessage.setCreationTick(MinecraftClient.getInstance().inGameHud.getTicks());
            }
            this.lines.add(0, newMessage);
            if (scrolledHeight > 0) {
                scrolledHeight += (HudConfigStorage.General.LINE_SPACE.config.getIntegerValue()) * newMessage.getLineCount() + HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
            }
            int visibleMessagesMaxSize =
                    HudConfigStorage.General.STORED_LINES.config.getIntegerValue();
            while (this.lines.size() > visibleMessagesMaxSize) {
                this.lines.remove(this.lines.size() - 1);
            }
        }
    }

    public int getConvertedX() {
        return (int) ((double) client.getWindow().getScaledWidth() * xPercent);
    }

    public int getConvertedY() {
        return (int) ((double) client.getWindow().getScaledHeight() * yPercent);
    }

    public int getConvertedWidth() {
        return (int) ((double) client.getWindow().getScaledWidth() * widthPercent);
    }

    public int getConvertedHeight() {
        return (int) ((double) client.getWindow().getScaledHeight() * heightPercent);
    }

    public int getTotalLines() {
        int count = 0;
        for (ChatMessage line : lines) {
            count += line.getLineCount();
        }
        return count;
    }

    public void scroll(double amount) {
        this.scrollEnd = this.scrolledHeight + amount * 15;
        this.scrollStart = this.scrolledHeight;
        lastScroll = Util.getMeasuringTimeMs();
    }

    public void updateScroll() {
        long time = Util.getMeasuringTimeMs();
        scrollDuration = 300;
        scrolledHeight = scrollStart + (
                (scrollEnd - scrollStart) * (1 - EasingMethod.Method.QUART.apply(
                        1 - ((float) (time - lastScroll)) / scrollDuration
                ))
        );
        int totalHeight = getTotalHeight();
        if (this.scrolledHeight > totalHeight) {
            scrollStart = totalHeight;
            scrollEnd = totalHeight;
            lastScroll = 0;
            this.scrolledHeight = totalHeight;
        }

        if (this.scrolledHeight <= 0) {
            scrollStart = 0;
            scrollEnd = 0;
            lastScroll = 0;
            this.scrolledHeight = 0;
        }
    }

    public static void drawRect(
            MatrixStack stack, int x, int y, int width, int height, int color) {
        DrawableHelper.fill(stack, x, y, x + width, y + height, color);
    }

    public static void fill(MatrixStack stack, int x, int y, int x2, int y2, int color) {
        DrawableHelper.fill(stack, x, y, x2, y2, color);
    }

    private static void drawOutline(
            MatrixStack stack, int x, int y, int width, int height, int color) {
        drawRect(stack, x, y, 1, height, color);
        drawRect(stack, x + width - 1, y, 1, height, color);
        drawRect(stack, x + 1, y, width - 2, 1, color);
        drawRect(stack, x + 1, y + height - 1, width - 2, 1, color);
    }

    public void resetScroll() {
        this.lastScroll = 0;
        this.scrollStart = 0;
        this.scrollEnd = 0;
        this.scrolledHeight = 0;
    }

    public int getPaddedWidth() {
        return (getScaledWidth()
                - HudConfigStorage.General.LEFT_PAD.config.getIntegerValue()
                - HudConfigStorage.General.RIGHT_PAD.config.getIntegerValue()
                - headOffset());
    }

    private int headOffset() {
        return HudConfigStorage.General.CHAT_HEADS.config.getBooleanValue() ? 10 : 0;
    }

    private int getActualY(int y) {
        return (int) Math.ceil(this.getConvertedY() / getScale()) - y;
    }

    private int getLeftX() {
        return (int) Math.ceil(this.getConvertedX() / getScale());
    }

    private int getPaddedLeftX() {
        return (getLeftX()
                + (int)
                        Math.ceil(
                                HudConfigStorage.General.LEFT_PAD.config.getIntegerValue()
                                        + (renderRight ? 0 : headOffset())));
    }

    private double getScale() {
        return HudConfigStorage.General.CHAT_SCALE.config.getDoubleValue();
    }

    private int getRightX() {
        return getLeftX() + getScaledWidth();
    }

    private int getPaddedRightX() {
        return (getRightX() - HudConfigStorage.General.RIGHT_PAD.config.getIntegerValue() - (renderRight ? headOffset() : 0));
    }

    public int getActualHeight() {
        return getConvertedHeight() + getBarHeight();
    }

    private int getScaledHeight() {
        return (int) Math.ceil(getConvertedHeight() / getScale());
    }

    private int getScaledWidth() {
        return (int) Math.ceil(getConvertedWidth() / getScale());
    }

    private int getBarHeight() {
        return 14;
    }

    private int getScaledBarHeight() {
        return (int) Math.ceil(14 * getScale());
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        int x = getConvertedX();
        int y = getConvertedY();
        return (x <= mouseX
                && x + getConvertedWidth() >= mouseX
                && y >= mouseY
                && y - getActualHeight() <= mouseY);
    }

    public int getTotalHeight() {
        return getTotalLines() * HudConfigStorage.General.LINE_SPACE.config.getIntegerValue() + (lines.size() - 1) * HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
    }

    public void render(MatrixStack matrixStack, int ticks, boolean focused) {
        if (!focused) {
            resetScroll();
        }
        updateScroll();
        if (visibility == HudConfigStorage.Visibility.FOCUSONLY && !focused) {
            return;
        }

        int totalLines = getTotalLines();

        boolean chatFocused = visibility == HudConfigStorage.Visibility.ALWAYS || focused;
        int totalHeight = getTotalHeight();

        if (scrolledHeight > totalHeight) {
            scrolledHeight = totalHeight;
        }

        matrixStack.push();
        matrixStack.scale((float) getScale(), (float) getScale(), 1);

        int lines = 0;
        int currentHeight = 0;
        int renderedLines = 0;
        int scaledWidth = getScaledWidth();
        int scaledHeight = getScaledHeight();
        int leftX = getLeftX();
        int padLX = getPaddedLeftX();
        int rightX = getRightX();
        int padRX = getPaddedRightX();
        int limit = getScaledHeight() - HudConfigStorage.General.TOP_PAD.config.getIntegerValue() + HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
        int lastY = 0;
        LimitedInteger y =
                new LimitedInteger(
                        getScaledHeight() - HudConfigStorage.General.TOP_PAD.config.getIntegerValue() + (HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue() + HudConfigStorage.General.LINE_SPACE.config.getIntegerValue() * (renderTopFirst ? 2 : 1)),
                        renderTopFirst ? HudConfigStorage.General.TOP_PAD.config.getIntegerValue() + HudConfigStorage.General.LINE_SPACE.config.getIntegerValue(): HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue());

        double scale = client.getWindow().getScaleFactor();
        ScissorUtil.applyScissorBox(
                (int) (getConvertedX() * scale),
                (int) ((client.getWindow().getScaledHeight() - getConvertedY()) * scale),
                (int) (getConvertedWidth() * scale),
                (int) (getConvertedHeight() * scale));
        boolean foundScroll = false;
        for (int j = 0; j < this.lines.size(); j++) {
            ChatMessage message = this.lines.get(j);
            // To get the proper index of reversed
            for (int i = message.getLineCount() - 1; i >= 0; i--) {
                int lineIndex = renderTopFirst ? i : message.getLineCount() - i - 1;
                lines++;
                if (currentHeight < scrolledHeight - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue()) {
                    currentHeight += HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                    continue;
                }
                boolean renderBelow = false;
                if (!foundScroll) {
                    foundScroll = true;
                    y.incrementIfPossible(currentHeight - (int) scrolledHeight - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue() + client.textRenderer.fontHeight);
                    renderBelow = true;
                }
                currentHeight += HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                if (!y.incrementIfPossible(HudConfigStorage.General.LINE_SPACE.config.getIntegerValue())) {
                    break;
                }
                ChatMessage.AdvancedChatLine line = message.getLines().get(renderTopFirst ? message.getLineCount() - i - 1 : i);
                drawLine(
                        matrixStack,
                        line,
                        leftX,
                        renderTopFirst ? limit - y.getValue() + client.textRenderer.fontHeight : y.getValue(),
                        padLX,
                        padRX,
                        lineIndex,
                        j,
                        renderedLines,
                        chatFocused,
                        ticks,
                        renderBelow ? y.getValue() - lastY - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue() - 1 : 0);
                lastY = y.getValue();
                renderedLines++;
            }
            if (currentHeight >= scrolledHeight) {
                if (lines == totalLines) {
                    break;
                }
                if (!y.isPossible(HudConfigStorage.General.LINE_SPACE.config.getIntegerValue() + HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue()) || !y.incrementIfPossible(HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue())) {
                    break;
                }
            }
            currentHeight += HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
        }
        ScissorUtil.resetScissor();
        if (renderedLines == 0) {
            y.setValue(0);
        }

        if (focused) {
            if (isSelected()) {
                tab.resetUnread();
            }
        }

        if (focused && !isMinimalist()) {
            drawOutline(
                    matrixStack,
                    leftX,
                    getActualY(0) - scaledHeight - 1,
                    scaledWidth,
                    scaledHeight + 1,
                    tab.getBorderColor().color());
            int scaledBar = getBarHeight();
            int newY = getScaledHeight() + scaledBar;
            String label = tab.getAbbreviation();
            int labelWidth = StringUtils.getStringWidth(label) + 8;
            drawRect(
                    matrixStack,
                    leftX,
                    getActualY(newY),
                    labelWidth,
                    scaledBar,
                    tab.getMainColor().color());
            drawOutline(
                    matrixStack,
                    leftX,
                    getActualY(newY),
                    labelWidth,
                    scaledBar,
                    tab.getBorderColor().color());
            DrawableHelper.drawCenteredText(
                    matrixStack,
                    MinecraftClient.getInstance().textRenderer,
                    tab.getAbbreviation(),
                    leftX + (labelWidth) / 2,
                    getActualY(newY - 3),
                    Colors.getInstance().getColorOrWhite("white").color());
            drawRect(
                    matrixStack,
                    leftX + labelWidth,
                    getActualY(newY),
                    getScaledWidth() - labelWidth,
                    scaledBar,
                    selected ? tab.getMainColor().color() : tab.getInnerColor().color());
            drawOutline(
                    matrixStack,
                    leftX + labelWidth,
                    getActualY(newY),
                    getScaledWidth() - labelWidth,
                    scaledBar,
                    tab.getBorderColor().color());

            drawOutline(
                    matrixStack,
                    rightX - scaledBar,
                    getActualY(newY),
                    scaledBar,
                    scaledBar,
                    tab.getBorderColor().color());
            drawOutline(
                    matrixStack,
                    rightX - scaledBar * 2 + 1,
                    getActualY(newY),
                    scaledBar,
                    scaledBar,
                    tab.getBorderColor().color());
            drawOutline(
                    matrixStack,
                    rightX - scaledBar * 3 + 2,
                    getActualY(newY),
                    scaledBar,
                    scaledBar,
                    tab.getBorderColor().color());

            // Close
            RenderUtils.color(1, 1, 1, 1);
            RenderUtils.bindTexture(X_ICON);
            DrawableHelper.drawTexture(
                    matrixStack,
                    rightX - scaledBar + 1,
                    getActualY(newY - 1),
                    scaledBar - 2,
                    scaledBar - 2,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32);

            // Resize
            RenderUtils.color(1, 1, 1, 1);
            RenderUtils.bindTexture(RESIZE_ICON);
            DrawableHelper.drawTexture(
                    matrixStack,
                    rightX - scaledBar * 2 + 2,
                    getActualY(newY - 1),
                    scaledBar - 2,
                    scaledBar - 2,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32);

            // Visibility
            RenderUtils.bindTexture(visibility.getTexture());
            DrawableHelper.drawTexture(
                    matrixStack,
                    rightX - scaledBar * 3 + 3,
                    getActualY(newY - 1),
                    scaledBar - 2,
                    scaledBar - 2,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32);

            double mouseX = client.mouse.getX() / 2;
            double mouseY = client.mouse.getY() / 2;
            if (isMouseOverVisibility(mouseX, mouseY)) {
                DrawableHelper.drawStringWithShadow(
                        matrixStack,
                        client.textRenderer,
                        visibility.getDisplayName(),
                        (int) (mouseX / getScale() + 4),
                        (int) (mouseY / getScale() - 16),
                        Colors.getInstance().getColorOrWhite("white").color());
            }
        }

        if (chatFocused) {
            if (y.getValue() < getScaledHeight()) {
                // Check to see if we've already gone above the boundaries
                fill(
                        matrixStack,
                        leftX,
                        getActualY(renderTopFirst ? limit - y.getValue() : y.getValue()),
                        rightX,
                        getActualY(renderTopFirst ? 0 : getScaledHeight()),
                        tab.getInnerColor().color());
            }
            // Scroll bar
            float add = (float) (scrolledHeight) / (getTotalHeight());
            int scrollHeight = (int) (add * (getScaledHeight() - 10));
            drawRect(
                    matrixStack,
                    getScaledWidth() + leftX - 1,
                    getActualY(scrollHeight + 10),
                    1,
                    10,
                    Colors.getInstance().getColorOrWhite("white").color());
        }
        matrixStack.pop();
    }

    private void drawLine(
            MatrixStack matrixStack,
            ChatMessage.AdvancedChatLine line,
            int x,
            int y,
            int pLX,
            int pRX,
            int lineIndex,
            int messageIndex,
            int renderedLines,
            boolean focused,
            int ticks,
            int renderBelow) {
        int height = HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
        if (renderedLines == 0) {
            if (focused) {
                height += renderTopFirst ? HudConfigStorage.General.TOP_PAD.config.getIntegerValue() + HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue() + 1 : HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue();
                height += renderBelow;
            }
        } else if (lineIndex == 0) {
            height += HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
            // Start of a line
        }
        Color background = line.getParent().getBackgroundColor();
        Color text = HudConfigStorage.General.EMPTY_TEXT_COLOR.config.get();
        if (background == null) {
            background = tab.getInnerColor();
        }
        if (messageIndex % 2 == 0
                && HudConfigStorage.General.ALTERNATE_LINES.config.getBooleanValue()) {
            if (background.alpha() <= 215) {
                background = background.withAlpha(background.alpha() + 40);
            } else {
                background = background.withAlpha(background.alpha() - 40);
            }
        }
        float applied = 1;
        if (!focused) {
            // Find fade percentage
            int fadeStart = HudConfigStorage.General.FADE_START.config.getIntegerValue();
            int fadeStop = fadeStart + HudConfigStorage.General.FADE_TIME.config.getIntegerValue();
            int timeAlive = ticks - line.getParent().getCreationTick();
            float percent =
                    (float)
                            Math.min(
                                    1,
                                    (double) (timeAlive - fadeStart)
                                            / (double) (fadeStop - fadeStart));
            applied =
                    1
                            - (float)
                                    ((EasingMethod)
                                                    HudConfigStorage.General.FADE_TYPE.config
                                                            .getOptionListValue())
                                            .apply(percent);
            applied = Math.max(0, applied);
            if (applied <= 0) {
                return;
            }
            if (applied < 1) {
                // Adjust color for background and text due to fade
                background = ColorUtil.fade(background, applied);
                text = ColorUtil.fade(text, applied);
            }
        }

        // Get line
        Text render = line.getText();
        if (line.getParent().getStacks() > 0 && lineIndex == 0) {
            FluidText toPrint = new FluidText(render);
            Style style = Style.EMPTY;
            TextColor color =
                    TextColor.fromRgb(Colors.getInstance().getColorOrWhite("gray").color());
            style = style.withColor(color);
            toPrint.getRawTexts()
                    .add(new RawText(" (" + (line.getParent().getStacks() + 1) + ")", style));
            render = toPrint;
        }

        int backgroundWidth;
        int scaledWidth = getScaledWidth();
        int lineWidth = client.textRenderer.getWidth(render) + 2;

        if (!focused
                && HudConfigStorage.General.HUD_LINE_TYPE.config.getOptionListValue()
                        == HudConfigStorage.HudLineType.COMPACT) {
            backgroundWidth = lineWidth + headOffset();
        } else {
            backgroundWidth = scaledWidth;
        }

        // Draw background
        int backgroundY = getActualY(y);
        if (renderTopFirst && renderedLines == 0) {
            backgroundY -= 1 + HudConfigStorage.General.TOP_PAD.config.getIntegerValue();
        }
        if (renderRight) {
            drawRect(matrixStack, x + (scaledWidth - backgroundWidth), backgroundY, backgroundWidth, height, background.color());
        } else {
            drawRect(matrixStack, x, backgroundY, backgroundWidth, height, background.color());
        }
        if (lineIndex == line.getParent().getLineCount() - 1
                && line.getParent().getOwner() != null
                && HudConfigStorage.General.CHAT_HEADS.config.getBooleanValue()) {
            // Allow head to be transparent
            RenderUtils.setupBlend();
            // Draw head
            RenderSystem.setShaderColor(1, 1, 1, applied);
            RenderSystem.setShaderTexture(0, line.getParent().getOwner().getTexture());
            int headX;
            if (renderRight) {
                headX = pRX + 2;
            } else {
                headX = pLX - 10;
            }
            int headY = getActualY(y);
            DrawableHelper.drawTexture(
                    matrixStack, headX, headY, 8, 8, 8, 8, 8, 8, 64, 64);
            DrawableHelper.drawTexture(
                    matrixStack, headX, headY, 8, 8, 40, 8, 8, 8, 64, 64);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        client.textRenderer.drawWithShadow(
                matrixStack, render.asOrderedText(), renderRight ? pRX - lineWidth : pLX, getActualY(y) + 1, text.color());
    }

    public Style getText(double mouseX, double mouseY) {
        if (!WindowManager.getInstance().isChatFocused()) {
            return null;
        }
        double relX = mouseX;
        double relY = getConvertedY() - mouseY;
        double trueX = relX / getScale() - getPaddedLeftX();
        double trueY = relY / getScale();
        // Divide it by chat scale to get where it actually is
        if (trueX < 0.0D || trueY < 0.0D) {
            return null;
        }
        if (trueY > getScaledHeight() || trueX > getScaledWidth()) {
            return null;
        }

        int lineHeight = 0;
        boolean foundScroll = false;
        LimitedInteger y =
                new LimitedInteger(
                        getScaledHeight() + HudConfigStorage.General.LINE_SPACE.config.getIntegerValue(),
                        HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue());
        for (ChatMessage message : this.lines) {
            // To get the proper index of reversed
            for (int i = message.getLineCount() - 1; i >= 0; i--) {
                lineHeight += HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                if (lineHeight < scrolledHeight) {
                    continue;
                }
                if (!foundScroll) {
                    foundScroll = true;
                    trueY -= lineHeight - (int) scrolledHeight - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                }
                if (!y.incrementIfPossible(HudConfigStorage.General.LINE_SPACE.config.getIntegerValue())) {
                    break;
                }

                if (trueY <= y.getValue()
                        && trueY
                                >= y.getValue()
                                        - HudConfigStorage.General.LINE_SPACE.config
                                                .getIntegerValue()) {
                    ChatMessage.AdvancedChatLine line = message.getLines().get(i);
                    double truestX = trueX;
                    if (renderRight) {
                        truestX = trueX - (getScaledWidth() - line.getWidth()) + headOffset() + HudConfigStorage.General.RIGHT_PAD.config.getIntegerValue() + HudConfigStorage.General.LEFT_PAD.config.getIntegerValue();
                    }
                    return this.client
                            .textRenderer
                            .getTextHandler()
                            .getStyleAt(line.getText(), (int) truestX);
                }
            }
            if (lineHeight >= scrolledHeight) {
                if (lineHeight == getTotalHeight()) {
                    break;
                }
                if (!y.isPossible(
                                HudConfigStorage.General.LINE_SPACE.config.getIntegerValue()
                                        + HudConfigStorage.General.MESSAGE_SPACE.config
                                                .getIntegerValue())
                        || !y.incrementIfPossible(
                                HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue())) {
                    break;
                }
            }
            lineHeight += HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
        }
        return null;
    }

    public ChatMessage getMessage(double mouseX, double mouseY) {
        if (!WindowManager.getInstance().isChatFocused()) {
            return null;
        }
        double relX = mouseX;
        double relY = getConvertedY() - mouseY;
        if (renderTopFirst) {
            relY = getConvertedHeight() - relY;
        }
        double trueX = relX / getScale() - getPaddedLeftX();
        double trueY = relY / getScale();
        // Divide it by chat scale to get where it actually is
        if (trueX < 0.0D || trueY < 0.0D) {
            return null;
        }
        if (trueY > getScaledHeight() || trueX > getScaledWidth()) {
            return null;
        }

        int lineHeight = 0;
        boolean foundScroll = false;
        LimitedInteger y = new LimitedInteger(getScaledHeight() + HudConfigStorage.General.LINE_SPACE.config.getIntegerValue(), HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue());
        for (ChatMessage message : this.lines) {
            // To get the proper index of reversed
            for (int i = message.getLineCount() - 1; i >= 0; i--) {
                lineHeight += HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                if (lineHeight < scrolledHeight) {
                    continue;
                }
                if (!foundScroll) {
                    foundScroll = true;
                    trueY -= lineHeight - (int) scrolledHeight - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
                }
                if (!y.incrementIfPossible(HudConfigStorage.General.LINE_SPACE.config.getIntegerValue())) {
                    break;
                }
                if (trueY <= y.getValue() && trueY >= y.getValue() - HudConfigStorage.General.LINE_SPACE.config.getIntegerValue()) {
                    return message;
                }
            }
            if (lineHeight >= scrolledHeight) {
                if (lineHeight == getTotalHeight()) {
                    break;
                }
                if (!y.isPossible(
                        HudConfigStorage.General.LINE_SPACE.config.getIntegerValue()
                                + HudConfigStorage.General.MESSAGE_SPACE.config
                                .getIntegerValue())
                        || !y.incrementIfPossible(
                        HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue())) {
                    break;
                }
            }
            lineHeight += HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
        }
        return null;
    }

    public boolean isMouseOverDragBar(double mouseX, double mouseY) {
        int x = getConvertedX();
        int y = getConvertedY();
        int width = getConvertedWidth();
        int height = getConvertedHeight();
        return (isMouseOver(mouseX, mouseY)
                && mouseX <= x + width - (getScaledBarHeight() * 3)
                && mouseY <= y - height);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        int convX = getConvertedX();
        int width = getConvertedWidth();
        boolean onButtons =
                isMouseOverDragBar(mouseX - (getScaledBarHeight() * 3), mouseY)
                        && mouseX >= convX + width - getScaledBarHeight() * 3;
        if (!onButtons) {
            return false;
        }
        int x = width - (int) (mouseX - convX);
        // Visibility | Resize | Close
        if (x <= getScaledBarHeight()) {
            // Close
            WindowManager.getInstance().deleteWindow(this);
        } else if (x >= getScaledBarHeight() * 2) {
            // Visibility
            visibility = visibility.cycle(true);
        }
        this.client
                .getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    public boolean isMouseOverResize(double mouseX, double mouseY) {
        int x = getConvertedX();
        int y = getConvertedY();
        int width = getConvertedWidth();
        int height = getConvertedHeight();
        return (isMouseOver(mouseX, mouseY)
                && mouseX >= x + width - (getScaledBarHeight() * 2)
                && mouseX <= x + width - (getScaledBarHeight())
                && mouseY <= y - height);
    }

    public boolean isMouseOverVisibility(double mouseX, double mouseY) {
        int x = getConvertedX();
        int y = getConvertedY();
        int width = getConvertedWidth();
        int height = getConvertedHeight();
        return (isMouseOver(mouseX, mouseY)
                && mouseX >= x + width - (getScaledBarHeight() * 3)
                && mouseX <= x + width - (getScaledBarHeight() * 2)
                && mouseY <= y - height);
    }

    public void setDimensions(int width, int height) {
        this.widthPercent = (double) width / client.getWindow().getScaledWidth();
        this.heightPercent = (double) height / client.getWindow().getScaledHeight();
        for (ChatMessage m : lines) {
            m.formatChildren(getPaddedWidth());
        }
    }

    public void stackMessage(HudChatMessage message) {
        ChatMessage toRemove = null;
        for (ChatMessage line : lines) {
            if (message.getMessage().isSimilar(line)) {
                if (!ConfigStorage.General.CHAT_STACK_UPDATE.config.getBooleanValue()) {
                    // Just update the message and don't resend it
                    line.setStacks(message.getMessage().getStacks());
                    return;
                }
                toRemove = line;
                break;
            }
        }
        if (toRemove != null) {
            // Remove and then readd it with the updated stack information
            lines.remove(toRemove);
            addMessage(message, true, true);
        }
    }

    public void clearLines() {
        this.lines.clear();
    }

    public void removeMessage(ChatMessage remove) {
        ChatMessage toRemove = null;
        for (ChatMessage line : lines) {
            if (remove.isSimilar(line) && remove.getTime().equals(line.getTime())) {
                toRemove = line;
                break;
            }
        }
        if (toRemove != null) {
            lines.remove(toRemove);
        }
    }

    public void toggleMinimalist() {
        minimalist = !minimalist;
    }

    public static class ChatWindowSerializer implements IJsonSave<ChatWindow> {

        @Override
        public ChatWindow load(JsonObject obj) {
            if (!obj.has("tabuuid")) {
                return null;
            }
            String uuidEl = obj.get("tabuuid").getAsString();
            UUID uuid = UUID.fromString(uuidEl);
            AbstractChatTab tab = AdvancedChatHud.MAIN_CHAT_TAB.fromUUID(uuid);
            if (tab == null) {
                AdvancedChatHud.LOGGER.warn("Tab with UUID " + uuidEl + " could not be found!");
                return null;
            }
            ChatWindow window = new ChatWindow(tab);
            window.setSelected(obj.get("selected").getAsBoolean());
            if (obj.has("renderRight")) {
                window.setRenderRight(obj.get("renderRight").getAsBoolean());
            }
            if (obj.has("renderTopFirst")) {
                window.setRenderTopFirst(obj.get("renderTopFirst").getAsBoolean());
            }
            if (obj.has("minimalist")) {
                window.setMinimalist(obj.get("minimalist").getAsBoolean());
            }
            window.setRelativePosition(obj.get("x").getAsDouble(), obj.get("y").getAsDouble());
            window.setVisibility(
                    HudConfigStorage.Visibility.fromVisibilityString(
                            obj.get("visibility").getAsString()));
            window.setRelativeDimensions(
                    obj.get("width").getAsDouble(), obj.get("height").getAsDouble());
            return window;
        }

        @Override
        public JsonObject save(ChatWindow chatWindow) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", chatWindow.getXPercent());
            obj.addProperty("y", chatWindow.getYPercent());
            obj.addProperty("width", chatWindow.getWidthPercent());
            obj.addProperty("height", chatWindow.getHeightPercent());
            obj.addProperty("visibility", chatWindow.getVisibility().getStringValue());
            obj.addProperty("tabuuid", chatWindow.getTab().getUuid().toString());
            obj.addProperty("selected", chatWindow.isSelected());
            obj.addProperty("renderRight", chatWindow.isRenderRight());
            obj.addProperty("renderTopFirst", chatWindow.isRenderTopFirst());
            obj.addProperty("minimalist", chatWindow.isMinimalist());
            return obj;
        }
    }

    public void onResolutionChange() {
        for (ChatMessage m : lines) {
            m.formatChildren(getConvertedWidth());
        }
    }
}
