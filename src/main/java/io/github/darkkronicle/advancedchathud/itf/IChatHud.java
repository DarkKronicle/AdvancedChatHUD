package io.github.darkkronicle.advancedchathud.itf;

import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import net.minecraft.client.MinecraftClient;

public interface IChatHud {

    AbstractChatTab getTab();

    void setTab(AbstractChatTab tab);

    void addMessage(HudChatMessage message);

    void clear(boolean clearHistory);


    static IChatHud getInstance() {
        return (IChatHud) MinecraftClient.getInstance().inGameHud.getChatHud();
    }
}
