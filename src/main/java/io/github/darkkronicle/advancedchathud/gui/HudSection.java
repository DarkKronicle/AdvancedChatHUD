/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.gui;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.ChatHistory;
import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.gui.ContextMenu;
import io.github.darkkronicle.advancedchatcore.gui.IconButton;
import io.github.darkkronicle.advancedchatcore.interfaces.AdvancedChatScreenSection;
import io.github.darkkronicle.advancedchatcore.util.*;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.HudChatMessageHolder;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import io.github.darkkronicle.advancedchathud.tabs.CustomChatTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Environment(EnvType.CLIENT)
public class HudSection extends AdvancedChatScreenSection {

    private static final Identifier ADD_ICON =
            new Identifier(AdvancedChatHud.MOD_ID, "textures/gui/chatwindow/add_window.png");

    private static final Identifier RESET_ICON =
            new Identifier(AdvancedChatHud.MOD_ID, "textures/gui/chatwindow/reset_windows.png");

    private ContextMenu menu = null;

    private ChatMessage message = null;

    public HudSection(AdvancedChatScreen screen) {
        super(screen);
    }

    private Color getColor() {
        Color baseColor;
        ChatWindow sel = WindowManager.getInstance().getSelected();
        if (sel == null) {
            baseColor = HudConfigStorage.MAIN_TAB.getInnerColor().config.get();
        } else {
            baseColor = sel.getTab().getInnerColor();
        }
        return baseColor;
    }

    @Override
    public void initGui() {
        boolean left = !HudConfigStorage.General.TAB_BUTTONS_ON_RIGHT.config.getBooleanValue();
        List<AbstractChatTab> tabs = new ArrayList<>(AdvancedChatHud.MAIN_CHAT_TAB.getAllChatTabs());
        if (!left) {
            Collections.reverse(tabs);
        }
        RowList<ButtonBase> rows = left ? getScreen().getLeftSideButtons() : getScreen().getRightSideButtons();
        rows.createSection("tabs", 0);
        for (AbstractChatTab tab : tabs) {
            rows.add("tabs", TabButton.fromTab(tab, 0, 0));
        }
        IconButton window = new IconButton(0, 0, 14, 32, ADD_ICON, (button) -> WindowManager.getInstance().onTabAddButton(IChatHud.getInstance().getTab()));
        IconButton reset = new IconButton(0, 0, 14, 32, RESET_ICON, (button) -> WindowManager.getInstance().reset());
        if (left) {
            rows.add("tabs", window);
            rows.add("tabs", reset);
        } else {
            rows.add("tabs", window, 0);
            rows.add("tabs", reset, 0);
        }

        if (getScreen().getChatField().getText().isEmpty()) {
            ChatWindow chatWindow = WindowManager.getInstance().getSelected();
            if (chatWindow == null) {
                return;
            }
            AbstractChatTab tab = chatWindow.getTab();
            if (tab instanceof CustomChatTab custom) {
                getScreen().getChatField().setText(custom.getStartingMessage());
                getScreen().getChatField().setCursor(custom.getStartingMessage().length());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        if (menu != null) {
            menu.render(mouseX, mouseY, true, context);
        }
    }

    public void createContextMenu(int mouseX, int mouseY) {
        LinkedHashMap<Text, ContextMenu.ContextConsumer> actions = new LinkedHashMap<>();
        message = WindowManager.getInstance().getMessage(mouseX, mouseY);
        if (message != null) {
            TextBuilder data = new TextBuilder();
            try {
                data.append(
                        message.getTime().format(DateTimeFormatter.ofPattern(ConfigStorage.General.TIME_FORMAT.config.getStringValue())), Style.EMPTY.withFormatting(Formatting.AQUA)
                );
            } catch (IllegalArgumentException e) {
                AdvancedChatHud.LOGGER.log(Level.WARN, "Can't format time for context menu!", e);
            }
            if (message.getOwner() != null) {
                data.append(" - ", Style.EMPTY.withFormatting(Formatting.GRAY));
                if (message.getOwner().getEntry().getDisplayName() != null) {
                    data.append(message.getOwner().getEntry().getDisplayName());
                } else {
                    data.append(message.getOwner().getEntry().getProfile().getName());
                }
            }
            if (!data.build().getString().isBlank())  {
                actions.put(data.build(), (x, y) -> {
                    InfoUtils.printActionbarMessage("advancedchathud.context.nothing");
                });
            }
            actions.put(Text.literal(StringUtils.translate("advancedchathud.context.copy")), (x, y) -> {
                MinecraftClient.getInstance().keyboard.setClipboard(message.getOriginalText().getString());
                InfoUtils.printActionbarMessage("advancedchathud.context.copied");
            });
            actions.put(Text.literal(StringUtils.translate("advancedchathud.context.delete")), (x, y) -> {
                HudChatMessageHolder.getInstance().removeChatMessage(message);
            });
            if (message.getOwner() != null) {
                actions.put(Text.literal(StringUtils.translate("advancedchathud.context.messageowner")), (x, y) -> {
                    getScreen().getChatField().setText("/msg " + message.getOwner().getEntry().getProfile().getName() + " ");
                });
            }
        }
        ChatWindow hovered = WindowManager.getInstance().getHovered(mouseX, mouseY);
        actions.put(Text.literal(StringUtils.translate("advancedchathud.context.removeallwindows")), (x, y) -> WindowManager.getInstance().reset());
        actions.put(Text.literal(StringUtils.translate("advancedchathud.context.clearallmessages")), (x, y) -> WindowManager.getInstance().clear());
        if (hovered != null) {
            actions.put(Text.literal(StringUtils.translate("advancedchathud.context.duplicatewindow")), (x, y) -> WindowManager.getInstance().duplicateTab(hovered, x, y));
            actions.put(Text.literal(StringUtils.translate("advancedchathud.context.configurewindow")), (x, y) -> WindowManager.getInstance().configureTab(getScreen(), hovered));
            actions.put(Text.literal(StringUtils.translate("advancedchathud.context.minimalist")), (x, y) -> hovered.toggleMinimalist());
        }
        menu = new ContextMenu(mouseX, mouseY, actions, () -> menu = null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            createContextMenu((int) mouseX, (int) mouseY);
            return true;
        }
        if (menu != null && menu.onMouseClicked((int) mouseX, (int) mouseY, button)) {
            return true;
        }
        return WindowManager.getInstance().mouseClicked(getScreen(), mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return WindowManager.getInstance().mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return WindowManager.getInstance().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 1.0D) {
            amount = 1.0D;
        }

        if (amount < -1.0D) {
            amount = -1.0D;
        }
        if (!Screen.hasShiftDown()) {
            amount *= 7.0D;
        }
        return WindowManager.getInstance().scroll(amount, mouseX, mouseY);
    }
}
