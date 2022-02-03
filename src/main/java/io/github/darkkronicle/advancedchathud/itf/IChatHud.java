/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.itf;

import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public interface IChatHud {

    AbstractChatTab getTab();

    void setTab(AbstractChatTab tab);

    void addMessage(HudChatMessage message);

    void clear(boolean clearHistory);

    boolean isOver(double mouseX, double mouseY);

    static IChatHud getInstance() {
        return (IChatHud) MinecraftClient.getInstance().inGameHud.getChatHud();
    }

    void removeMessage(ChatMessage remove);
}
