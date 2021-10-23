package io.github.darkkronicle.advancedchathud;

import io.github.darkkronicle.advancedchatcore.chat.ChatMessage;
import io.github.darkkronicle.advancedchatcore.interfaces.IChatMessageProcessor;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.gui.WindowManager;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HudChatMessageHolder implements IChatMessageProcessor {

    @Getter
    private final List<HudChatMessage> messages = new ArrayList<>();

    private static final HudChatMessageHolder INSTANCE = new HudChatMessageHolder();

    private HudChatMessageHolder() {}

    public static HudChatMessageHolder getInstance() {
        return INSTANCE;
    }

    @Override
    public void onMessageUpdate(ChatMessage message, UpdateType type) {
        if (type == UpdateType.ADDED) {
            addMessage(new HudChatMessage(message));
        } else if (type == UpdateType.REMOVE) {
            remove(message);
        } else if (type == UpdateType.STACK) {
            HudChatMessage m = getMessage(message);
            if (m != null) {
                WindowManager.getInstance().onStackedMessage(m);
            }
        }
    }

    public void addMessage(HudChatMessage message) {
        messages.add(0, message);
        WindowManager.getInstance().onNewMessage(message);
        while (
            messages.size() >
            HudConfigStorage.General.STORED_LINES.config.getIntegerValue()
        ) {
            messages.remove(messages.size() - 1);
        }
    }

    public void clear() {
        this.messages.clear();
    }

    public void remove(ChatMessage message) {
        HudChatMessage remove = getMessage(message);
        if (remove != null) {
            messages.remove(remove);
        }
    }

    public HudChatMessage getMessage(ChatMessage message) {
        for (HudChatMessage m : messages) {
            if (m.getMessage().getUuid().equals(message.getUuid())) {
                return m;
            }
        }
        return null;
    }
}
