/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.tabs;

import com.google.gson.JsonArray;
import fi.dy.masa.malilib.util.FileUtils;
import io.github.darkkronicle.Konstruct.NodeException;
import io.github.darkkronicle.Konstruct.functions.Function;
import io.github.darkkronicle.Konstruct.nodes.Node;
import io.github.darkkronicle.Konstruct.parser.*;
import io.github.darkkronicle.Konstruct.type.NullObject;
import io.github.darkkronicle.advancedchatcore.konstruct.AdvancedChatKonstruct;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.HudChatMessage;
import io.github.darkkronicle.advancedchathud.HudChatMessageHolder;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.HudConfigStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.darkkronicle.advancedchathud.gui.WindowManager;
import io.github.darkkronicle.advancedchathud.util.FileUtil;
import lombok.Getter;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;

/** Main chat tab that manages other chat tabs. */
public class MainChatTab extends AbstractChatTab {

    @Getter private ArrayList<AbstractChatTab> allChatTabs = new ArrayList<>();

    @Getter private ArrayList<CustomChatTab> customChatTabs = new ArrayList<>();

    public static boolean LOAD_ALL_JSON = false;

    @Getter
    private NodeProcessor processor = null;

    public MainChatTab() {
        super(
                "Main",
                HudConfigStorage.MAIN_TAB.getAbbreviation().config.getStringValue(),
                HudConfigStorage.MAIN_TAB.getMainColor().config.get(),
                HudConfigStorage.MAIN_TAB.getBorderColor().config.get(),
                HudConfigStorage.MAIN_TAB.getInnerColor().config.get(),
                HudConfigStorage.MAIN_TAB.getShowUnread().config.getBooleanValue(),
                HudConfigStorage.MAIN_TAB.getUuid());
        setUpTabs();
    }

    public void refreshOptions() {
        this.abbreviation = HudConfigStorage.MAIN_TAB.getAbbreviation().config.getStringValue();
        this.mainColor = HudConfigStorage.MAIN_TAB.getMainColor().config.get();
        this.innerColor = HudConfigStorage.MAIN_TAB.getInnerColor().config.get();
        this.borderColor = HudConfigStorage.MAIN_TAB.getBorderColor().config.get();
        this.showUnread = HudConfigStorage.MAIN_TAB.getShowUnread().config.getBooleanValue();
        this.uuid = HudConfigStorage.MAIN_TAB.getUuid();
    }

    public CustomChatTab getCustom(String name) {
        for (CustomChatTab tab : customChatTabs) {
            if (tab.getName().equals(name)) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public boolean shouldAdd(Text text) {
        return true;
    }

    /** Method used for loading in tabs from the config. */
    public void setUpTabs() {
        JsonArray windows = null;
        if (!LOAD_ALL_JSON) {
            windows = WindowManager.getInstance().saveJson();
        }
        customChatTabs = new ArrayList<>();
        allChatTabs = new ArrayList<>();
        allChatTabs.add(this);
        for (ChatTab tab : HudConfigStorage.TABS) {
            CustomChatTab customTab = new CustomChatTab(tab);
            customChatTabs.add(customTab);
            allChatTabs.add(customTab);
        }

        Path konstructDir = FileUtils.getConfigDirectory().toPath().resolve("advancedchat").resolve("konstructTabs");
        konstructDir.toFile().mkdirs();

        processor = AdvancedChatKonstruct.getInstance().copy();
        processor.addFunction("getTab", new Function() {
            @Override
            public Result parse(ParseContext context, List<Node> input) {
                CustomChatTab tab = getCustom(Function.parseArgument(context, input, 0).getContent().getString());
                if (tab == null) {
                    return Result.success(new NullObject());
                }
                return Result.success(new ChatTabObject(tab));
            }

            @Override
            public IntRange getArgumentCount() {
                return IntRange.of(1);
            }
        });

        Optional<List<Path>> files = FileUtil.getFilesWithExtensionCaught(konstructDir, ".knst");
        if (files.isPresent() && files.get().size() != 0) {
            this.loadKonstruct(files.get());
        }

        for (HudChatMessage message : HudChatMessageHolder.getInstance().getMessages()) {
            ArrayList<AbstractChatTab> tabs = new ArrayList<>();
            for (AbstractChatTab t : allChatTabs) {
                if (t.shouldAdd(message.getMessage().getOriginalText())) {
                    tabs.add(t);
                }
            }
            message.setTabs(tabs);
        }
        this.refreshOptions();
        if (windows != null) {
            WindowManager.getInstance().loadFromJson(windows);
        }
    }

    public void loadKonstruct(List<Path> paths) {
        for (Path path : paths) {
            try {
                loadKonstruct(path);
            } catch (IOException e) {
                AdvancedChatHud.LOGGER.log(Level.ERROR, "Error reading " + path + ".", e);
            } catch (NodeException e) {
                AdvancedChatHud.LOGGER.log(Level.ERROR, "Error setting up konstruct script " + path, e);
            }
        }
    }

    private void loadKonstruct(Path path) throws IOException, NodeException {
        String contents = String.join("\n", Files.readAllLines(path)).replaceAll("\r", "");
        Node node = AdvancedChatKonstruct.getInstance().getNode(contents);
        processor.parse(node);
    }

    public AbstractChatTab fromUUID(UUID uuid) {
        for (AbstractChatTab tab : allChatTabs) {
            if (tab.getUuid().equals(uuid)) {
                return tab;
            }
        }
        return null;
    }
}
