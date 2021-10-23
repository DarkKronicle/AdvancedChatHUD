package io.github.darkkronicle.advancedchathud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import net.minecraft.client.gui.screen.Screen;

/**
 * Screen for importing and exporting {@link ChatTab}.
 */
public class SharingScreen extends GuiBase {

    private final String starting;
    private static final Gson GSON = new GsonBuilder().create();
    private GuiTextFieldGeneric text;

    public SharingScreen(String starting, Screen parent) {
        this.setParent(parent);
        this.setTitle(StringUtils.translate("advancedchat.gui.menu.import"));
        this.starting = starting;
    }

    /**
     * Creates a SharingScreen from a tab
     */
    public static SharingScreen fromTab(ChatTab tab, Screen parent) {
        ChatTab.ChatTabJsonSave tabJsonSave = new ChatTab.ChatTabJsonSave();
        return new SharingScreen(GSON.toJson(tabJsonSave.save(tab)), parent);
    }

    @Override
    public void init() {
        int x = this.width / 2 - 150;
        int y = 50;
        text = new GuiTextFieldGeneric(x, y, 300, 20, client.textRenderer);
        y -= 24;
        text.setMaxLength(12800);
        if (starting != null) {
            text.setText(starting);
            text.setTextFieldFocused(true);
        }
        text.changeFocus(true);
        text.setDrawsBackground(true);
        text.setEditable(true);
        text.changeFocus(true);
        this.addTextField(text, null);
        String tabName = ButtonListener.Type.IMPORT_TAB.getDisplayName();
        int tabWidth = StringUtils.getStringWidth(tabName) + 10;
        this.addButton(
                new ButtonGeneric(x, y, tabWidth, 20, tabName),
                new ButtonListener(ButtonListener.Type.IMPORT_TAB, this)
            );
    }

    private static class ButtonListener implements IButtonActionListener {

        public enum Type {
            IMPORT_TAB("importtab");

            public final String translationString;

            private static String translate(String key) {
                return "advancedchat.gui.button." + key;
            }

            Type(String key) {
                this.translationString = translate(key);
            }

            public String getDisplayName() {
                return StringUtils.translate(translationString);
            }
        }

        private final Type type;
        private final SharingScreen parent;

        public ButtonListener(Type type, SharingScreen parent) {
            this.type = type;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(
            ButtonBase button,
            int mouseButton
        ) {
            try {
                if (parent.text.getText().equals("")) {
                    throw new NullPointerException("Message can't be blank!");
                }
                if (type == Type.IMPORT_TAB) {
                    ChatTab.ChatTabJsonSave tabSave = new ChatTab.ChatTabJsonSave();
                    ChatTab tab = tabSave.load(
                        new JsonParser()
                            .parse(parent.text.getText())
                            .getAsJsonObject()
                    );
                    if (tab == null) {
                        throw new NullPointerException("Filter is null!");
                    }
                    HudConfigStorage.TABS.add(tab);
                    AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
                    parent.addGuiMessage(
                        Message.MessageType.SUCCESS,
                        5000,
                        StringUtils.translate(
                            "advancedchat.gui.message.successful"
                        )
                    );
                }
            } catch (Exception e) {
                parent.addGuiMessage(
                    Message.MessageType.ERROR,
                    10000,
                    StringUtils.translate("advancedchat.gui.message.error") +
                    ": " +
                    e.getMessage()
                );
            }
        }
    }
}
