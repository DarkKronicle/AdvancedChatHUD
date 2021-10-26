package io.github.darkkronicle.advancedchathud.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;

public class GuiTabEditor extends GuiConfigsBase {

    private final ChatTab tab;
    private final boolean main;

    public GuiTabEditor(Screen parent, ChatTab tab) {
        this(parent, tab, false);
    }

    public GuiTabEditor(Screen parent, ChatTab tab, boolean main) {
        super(
            10,
            50,
            AdvancedChatHud.MOD_ID,
            parent,
            tab.getName().config.getStringValue()
        );
        this.tab = tab;
        this.main = main;
        this.setParent(parent);
    }

    @Override
    public void initGui() {
        super.initGui();
        createButtons(10, 26);
    }

    private void createButtons(int x, int y) {
        String backText = ButtonListener.Type.BACK.getDisplayName();
        int backWidth = StringUtils.getStringWidth(backText) + 10;
        int topx = x;
        ButtonGeneric back = new ButtonGeneric(
            x + backWidth,
            y,
            backWidth,
            true,
            backText
        );
        this.addButton(
                back,
                new ButtonListener(ButtonListener.Type.BACK, this)
            );
        topx += back.getWidth() + 2;

        if (!main) {
            String matchesText = ButtonListener.Type.MATCHES.getDisplayName();
            int matchesWidth = StringUtils.getStringWidth(matchesText) + 10;
            ButtonGeneric match = new ButtonGeneric(
                topx + matchesWidth,
                y,
                matchesWidth,
                true,
                matchesText
            );
            this.addButton(
                    match,
                    new ButtonListener(ButtonListener.Type.MATCHES, this)
                );
            topx += match.getWidth() + 2;
        }

        String exportText = ButtonListener.Type.EXPORT.getDisplayName();
        int exportWidth = StringUtils.getStringWidth(exportText) + 10;
        ButtonGeneric export = new ButtonGeneric(
            topx + exportWidth,
            y,
            exportWidth,
            true,
            exportText
        );
        this.addButton(
                export,
                new ButtonListener(ButtonListener.Type.EXPORT, this)
            );
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        ArrayList<IConfigBase> config = new ArrayList<>();
        List<ConfigStorage.SaveableConfig<? extends IConfigBase>> options;
        if (main) {
            options = tab.getMainEditableOptions();
        } else {
            options = tab.getOptions();
        }
        for (ConfigStorage.SaveableConfig<? extends IConfigBase> s : options) {
            config.add(s.config);
        }

        return GuiConfigsBase.ConfigOptionWrapper.createFor(config);
    }

    @Override
    public void onClose() {
        save();
        super.onClose();
    }

    @Override
    protected void closeGui(boolean showParent) {
        // Save the changes :)
        save();
        super.closeGui(showParent);
    }

    public void save() {
        AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();
    }

    public void back() {
        closeGui(true);
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        // Override so that on escape stuff still gets saved
        if (this.activeKeybindButton != null) {
            this.activeKeybindButton.onKeyPressed(keyCode);
            return true;
        } else {
            if (this.getListWidget().onKeyTyped(keyCode, scanCode, modifiers)) {
                return true;
            }

            if (
                keyCode == KeyCodes.KEY_ESCAPE &&
                this.parentScreen != GuiUtils.getCurrentScreen()
            ) {
                // Make sure to save
                closeGui(true);
                return true;
            }

            return false;
        }
    }

    public static class ButtonListener implements IButtonActionListener {

        private final GuiTabEditor parent;
        private final ButtonListener.Type type;

        public ButtonListener(ButtonListener.Type type, GuiTabEditor parent) {
            this.type = type;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(
            ButtonBase button,
            int mouseButton
        ) {
            if (this.type == ButtonListener.Type.BACK) {
                parent.back();
            } else if (this.type == ButtonListener.Type.EXPORT) {
                parent.save();
                GuiBase.openGui(SharingScreen.fromTab(parent.tab, parent));
            } else if (this.type == Type.MATCHES) {
                parent.save();
                GuiBase.openGui(new MatchesEditor(parent, parent.tab));
            }
        }

        public enum Type {
            BACK("back"),
            EXPORT("export"),
            MATCHES("matches");

            private final String translation;

            private static String translate(String key) {
                return "advancedchathud.gui.button." + key;
            }

            Type(String key) {
                this.translation = translate(key);
            }

            public String getDisplayName() {
                return StringUtils.translate(translation);
            }
        }
    }
}
