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
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
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

@Environment(EnvType.CLIENT)
public class ChatWindow {

    private int scrolledLines = 0;

    @Getter private double yPercent;

    @Getter private double xPercent;

    @Getter private double widthPercent;

    @Getter private double heightPercent;

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
            if (scrolledLines > 0) {
                scrolledLines++;
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
        this.scrolledLines = (int) ((double) this.scrolledLines + amount);
        int totalLines = getTotalLines();
        if (this.scrolledLines > totalLines) {
            this.scrolledLines = totalLines;
        }

        if (this.scrolledLines <= 0) {
            this.scrolledLines = 0;
        }
    }

    private static void drawRect(
            MatrixStack stack, int x, int y, int width, int height, int color) {
        DrawableHelper.fill(stack, x, y, x + width, y + height, color);
    }

    private static void fill(MatrixStack stack, int x, int y, int x2, int y2, int color) {
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
        this.scrolledLines = 0;
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
                                        + headOffset()));
    }

    private double getScale() {
        return HudConfigStorage.General.CHAT_SCALE.config.getDoubleValue();
    }

    private int getRightX() {
        return getLeftX() + getScaledWidth();
    }

    private int getPaddedRightX() {
        return (getRightX() - HudConfigStorage.General.RIGHT_PAD.config.getIntegerValue());
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

    public void render(MatrixStack matrixStack, int ticks, boolean focused) {
        if (visibility == HudConfigStorage.Visibility.FOCUSONLY && !focused) {
            return;
        }

        int lineCount = lines.size();
        int totalLines = getTotalLines();

        boolean chatFocused = visibility == HudConfigStorage.Visibility.ALWAYS || focused;

        if (scrolledLines > totalLines) {
            scrolledLines = totalLines;
        }

        matrixStack.push();
        matrixStack.scale((float) getScale(), (float) getScale(), 1);

        int lines = 0;
        int renderedLines = 0;
        int scaledWidth = getScaledWidth();
        int scaledHeight = getScaledHeight();
        int leftX = getLeftX();
        int padLX = getPaddedLeftX();
        int rightX = getRightX();
        int padRX = getPaddedRightX();
        LimitedInteger y =
                new LimitedInteger(
                        getScaledHeight()
                                - HudConfigStorage.General.TOP_PAD.config.getIntegerValue(),
                        HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue());

        for (int j = 0; j < this.lines.size(); j++) {
            ChatMessage message = this.lines.get(j);
            // To get the proper index of reversed
            for (int i = message.getLineCount() - 1; i >= 0; i--) {
                int lineIndex = message.getLineCount() - i - 1;
                lines++;
                if (lines < scrolledLines) {
                    continue;
                }
                if (!y.incrementIfPossible(
                        HudConfigStorage.General.LINE_SPACE.config.getIntegerValue())) {
                    break;
                }
                ChatMessage.AdvancedChatLine line = message.getLines().get(i);
                drawLine(
                        matrixStack,
                        line,
                        leftX,
                        y.getValue(),
                        padLX,
                        padRX,
                        lineIndex,
                        j,
                        renderedLines,
                        chatFocused,
                        ticks);
                renderedLines++;
            }
            if (lines >= scrolledLines) {
                if (lines == totalLines) {
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
        }
        if (renderedLines == 0) {
            y.setValue(0);
        }

        if (focused) {
            if (isSelected()) {
                tab.resetUnread();
            }
            drawOutline(
                    matrixStack,
                    leftX,
                    getActualY(0) - scaledHeight - 1,
                    scaledWidth,
                    scaledHeight + 1,
                    tab.getBorderColor().color());
            int scaledBar = getBarHeight();
            int newY = getScaledHeight() + scaledBar;
            String label = tab.getAbreviation();
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
                    tab.getBorderColor().withAlpha(180).color());
            DrawableHelper.drawCenteredText(
                    matrixStack,
                    MinecraftClient.getInstance().textRenderer,
                    tab.getAbreviation(),
                    leftX + (labelWidth) / 2,
                    getActualY(newY - 3),
                    ColorUtil.WHITE.color());
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
                        ColorUtil.WHITE.color());
            }
        }

        if (chatFocused) {
            fill(
                    matrixStack,
                    leftX,
                    getActualY(y.getValue()),
                    rightX,
                    getActualY(getScaledHeight()),
                    tab.getInnerColor().color());
            // Scroll bar
            float add = (float) (scrolledLines) / (totalLines + 1);
            int scrollHeight = (int) (add * getScaledHeight());
            drawRect(
                    matrixStack,
                    getScaledWidth() + leftX - 1,
                    getActualY(scrollHeight + 10),
                    1,
                    10,
                    ColorUtil.WHITE.color());
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
            int ticks) {
        int height = HudConfigStorage.General.LINE_SPACE.config.getIntegerValue();
        if (renderedLines == 0) {
            if (focused) {
                height += HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue();
            }
        } else if (lineIndex == 0) {
            height += HudConfigStorage.General.MESSAGE_SPACE.config.getIntegerValue();
            // Start of a line
        }
        ColorUtil.SimpleColor background = line.getParent().getBackground();
        ColorUtil.SimpleColor text =
                HudConfigStorage.General.EMPTY_TEXT_COLOR.config.getSimpleColor();
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
            TextColor color = TextColor.fromRgb(ColorUtil.GRAY.color());
            style = style.withColor(color);
            toPrint.getRawTexts()
                    .add(new RawText(" (" + (line.getParent().getStacks() + 1) + ")", style));
            render = toPrint;
        }

        int backgroundWidth;

        if (!focused
                && HudConfigStorage.General.HUD_LINE_TYPE.config.getOptionListValue()
                        == HudConfigStorage.HudLineType.COMPACT) {
            backgroundWidth = client.textRenderer.getWidth(render) + 4 + headOffset();
        } else {
            backgroundWidth = getScaledWidth();
        }

        // Draw background
        drawRect(matrixStack, x, getActualY(y), backgroundWidth, height, background.color());
        if (lineIndex == line.getParent().getLineCount() - 1
                && line.getParent().getOwner() != null
                && HudConfigStorage.General.CHAT_HEADS.config.getBooleanValue()) {
            RenderSystem.setShaderColor(1, 1, 1, applied);
            RenderSystem.setShaderTexture(0, line.getParent().getOwner().getTexture());
            DrawableHelper.drawTexture(
                    matrixStack, pLX - 10, getActualY(y), 8, 8, 8, 8, 8, 8, 64, 64);
            DrawableHelper.drawTexture(
                    matrixStack, pLX - 10, getActualY(y), 8, 8, 40, 8, 8, 8, 64, 64);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        client.textRenderer.drawWithShadow(
                matrixStack, render.asOrderedText(), pLX, getActualY(y) + 1, text.color());
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

        int lines = 0;
        int lineCount = this.lines.size();
        LimitedInteger y =
                new LimitedInteger(
                        getScaledHeight(),
                        HudConfigStorage.General.BOTTOM_PAD.config.getIntegerValue());
        for (ChatMessage message : this.lines) {
            // To get the proper index of reversed
            for (int i = message.getLineCount() - 1; i >= 0; i--) {
                lines++;
                if (lines < scrolledLines) {
                    continue;
                }
                if (!y.incrementIfPossible(
                        HudConfigStorage.General.LINE_SPACE.config.getIntegerValue())) {
                    break;
                }
                if (trueY <= y.getValue()
                        && trueY
                                >= y.getValue()
                                        - HudConfigStorage.General.LINE_SPACE.config
                                                .getIntegerValue()) {
                    ChatMessage.AdvancedChatLine line = message.getLines().get(i);
                    return this.client
                            .textRenderer
                            .getTextHandler()
                            .getStyleAt(line.getText(), (int) trueX);
                }
            }
            if (lines >= scrolledLines) {
                if (lines == lineCount) {
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
            m.formatChildren(getConvertedWidth());
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
            return obj;
        }
    }
}
