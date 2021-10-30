/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.mixin;

import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessageHolder;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.itf.IChatHud;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(value = ChatHud.class, priority = 1050)
@Environment(EnvType.CLIENT)
public abstract class MixinChatHud implements IChatHud {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine<Text>> messages;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;

    @Shadow private int scrolledLines;
    @Shadow private boolean hasUnreadNewMessages;

    private AbstractChatTab tab;


    @Shadow public abstract void reset();

    @Shadow public abstract int getWidth();

    @Shadow public abstract double getChatScale();

    @Shadow protected abstract boolean isChatFocused();

    @Shadow public abstract void scroll(double amount);

    @Shadow public abstract void clear(boolean clearHistory);

    @Override
    public void addMessage(HudChatMessage hudMsg) {
        if (tab == null || !hudMsg.getTabs().contains(tab)) return;
        tab.resetUnread();

        int width = MathHelper.floor((double)this.getWidth() / this.getChatScale());

        ChatMessage msg = hudMsg.getMessage();

        List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(msg.getDisplayText(), width, this.client.textRenderer);

        OrderedText orderedText;
        for (Iterator<OrderedText> text = list.iterator(); text.hasNext();
             this.visibleMessages.add(0, new ChatHudLine<>(msg.getCreationTick(), orderedText, msg.getId()))) {
            orderedText = text.next();
            if (this.isChatFocused() && this.scrolledLines > 0) {
                this.hasUnreadNewMessages = true;
                this.scroll(1.0D);
            }
        }

        while (this.visibleMessages.size() > HudConfigStorage.General.STORED_LINES.config.getIntegerValue()) {
            this.visibleMessages.remove(this.visibleMessages.size() - 1);
        }

        this.messages.add(0, new ChatHudLine<>(msg.getCreationTick(), msg.getDisplayText(), msg.getId()));
        while (this.messages.size() > HudConfigStorage.General.STORED_LINES.config.getIntegerValue()) {
            this.messages.remove(this.messages.size() - 1);
        }
    }

    public AbstractChatTab getTab() {
        return tab;
    }

    public void setTab(AbstractChatTab tab) {
        this.tab = tab;
        this.messages.clear();
        this.visibleMessages.clear();

        List<HudChatMessage> messages = HudChatMessageHolder.getInstance().getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            addMessage(messages.get(i));
        }
        this.reset();
    }

}
