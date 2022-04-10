package io.github.darkkronicle.advancedchathud.config.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import io.github.darkkronicle.advancedchatcore.gui.buttons.BackButtonListener;
import io.github.darkkronicle.advancedchatcore.gui.buttons.Buttons;
import io.github.darkkronicle.advancedchatcore.interfaces.IClosable;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;
import io.github.darkkronicle.advancedchathud.gui.ChatWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class ChatWindowEditor extends GuiConfigsBase implements IClosable {

    private final ChatWindow window;

    private final ConfigInteger windowX = new ConfigInteger(
            "advancedchathud.windowconfig.x",
            HudConfigStorage.General.X.config.getIntegerValue(), 0, MinecraftClient.getInstance().getWindow().getScaledWidth(),
            "advancedchathud.windowconfig.info.x");

    private final ConfigInteger windowY = new ConfigInteger(
            "advancedchathud.windowconfig.y",
            HudConfigStorage.General.Y.config.getIntegerValue(), 0, MinecraftClient.getInstance().getWindow().getScaledHeight(),
            "advancedchathud.windowconfig.info.y");

    private final ConfigInteger windowWidth = new ConfigInteger(
            "advancedchathud.windowconfig.width",
            HudConfigStorage.General.WIDTH.config.getIntegerValue(), 40, MinecraftClient.getInstance().getWindow().getScaledWidth(),
            "advancedchathud.windowconfig.info.width");

    private final ConfigInteger windowHeight = new ConfigInteger(
            "advancedchathud.windowconfig.height",
            HudConfigStorage.General.HEIGHT.config.getIntegerValue(), 40, MinecraftClient.getInstance().getWindow().getScaledHeight(),
            "advancedchathud.windowconfig.info.height");

    private final ConfigBoolean renderRight = new ConfigBoolean(
            "advancedchathud.windowconfig.renderright",
            false,
            "advancedchathud.windowconfig.info.renderright");

    private final ConfigBoolean minimalist = new ConfigBoolean(
            "advancedchathud.windowconfig.minimalist",
            false,
            "advancedchathud.windowconfig.info.minimalist");

    public ChatWindowEditor(Screen parent, ChatWindow window) {
        super(10, 50, AdvancedChatHud.MOD_ID, parent, "Chat Window");
        this.window = window;
        this.setParent(parent);
        getSettings();
    }

    public void getSettings() {
        windowX.setIntegerValue(window.getConvertedX());
        windowY.setIntegerValue(window.getConvertedY());
        windowWidth.setIntegerValue(window.getConvertedWidth());
        windowHeight.setIntegerValue(window.getConvertedHeight());
        renderRight.setBooleanValue(window.isRenderRight());
        minimalist.setBooleanValue(window.isMinimalist());
    }

    public void applySettings() {
        window.setPosition(windowX.getIntegerValue(), windowY.getIntegerValue());
        window.setDimensions(windowWidth.getIntegerValue(), windowHeight.getIntegerValue());
        window.setRenderRight(renderRight.getBooleanValue());
        window.setMinimalist(minimalist.getBooleanValue());
    }

    @Override
    public void initGui() {
        super.initGui();
        createButtons(10, 26);
    }

    private void createButtons(int x, int y) {
        ButtonGeneric back = Buttons.BACK.createButton(x, y);
        x += this.addButton(back, new BackButtonListener(this)).getWidth() + 2;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        ArrayList<IConfigBase> config = new ArrayList<>();
        config.add(windowX);
        config.add(windowY);
        config.add(windowWidth);
        config.add(windowHeight);
        config.add(renderRight);
        config.add(minimalist);

        return GuiConfigsBase.ConfigOptionWrapper.createFor(config);
    }

    @Override
    public void close() {
        save();
        super.close();
    }

    @Override
    protected void closeGui(boolean showParent) {
        // Save the changes :)
        save();
        super.closeGui(showParent);
    }

    public void save() {
        applySettings();
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

            if (keyCode == KeyCodes.KEY_ESCAPE
                    && this.parentScreen != GuiUtils.getCurrentScreen()) {
                // Make sure to save
                closeGui(true);
                return true;
            }

            return false;
        }
    }

    @Override
    public void close(ButtonBase button) {
        closeGui(true);
    }
}
