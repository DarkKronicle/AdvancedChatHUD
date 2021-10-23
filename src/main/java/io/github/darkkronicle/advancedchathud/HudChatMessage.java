package io.github.darkkronicle.advancedchathud;

import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchathud.tabs.AbstractChatTab;
import io.github.darkkronicle.advancedchathud.tabs.CustomChatTab;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HudChatMessage {

    @Setter
    @Getter
    private List<AbstractChatTab> tabs;

    @Getter
    private final ChatMessage message;

    public HudChatMessage(ChatMessage message) {
        this(message, new ArrayList<>());
        ArrayList<AbstractChatTab> added = new ArrayList<>();
        boolean forward = true;
        if (AdvancedChatHud.MAIN_CHAT_TAB.getCustomChatTabs().size() > 0) {
            for (CustomChatTab tab : AdvancedChatHud.MAIN_CHAT_TAB.getCustomChatTabs()) {
                if (!tab.shouldAdd(message.getOriginalText())) {
                    continue;
                }
                if (added.contains(tab)) {
                    continue;
                }
                added.add(tab);
                if (!tab.isForward()) {
                    forward = false;
                    break;
                }
            }
        }
        if (forward) {
            added.add(AdvancedChatHud.MAIN_CHAT_TAB);
        }
        for (AbstractChatTab tab : added) {
            tab.addNewUnread();
        }
        setTabs(added);
    }

    public HudChatMessage(ChatMessage message, List<AbstractChatTab> tabs) {
        this.message = message;
        this.tabs = tabs;
    }
}
