/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigSimpleColor;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.gui.WindowManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class HudConfigStorage implements IConfigHandler {

    public static final String CONFIG_FILE_NAME = AdvancedChatHud.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;
    public static final List<ChatTab> TABS = new ArrayList<>();
    public static ChatTab MAIN_TAB = ChatTab.getMainOptions();

    public static class General {

        public static final String NAME = "general";

        public static String translate(String key) {
            return StringUtils.translate("advancedchathud.config.general." + key);
        }

        public static final ConfigStorage.SaveableConfig<ConfigBoolean> CHAT_HEADS =
                ConfigStorage.SaveableConfig.fromConfig(
                        "chatHeads",
                        new ConfigBoolean(
                                translate("chatheads"), false, translate("info.chatheads")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> WIDTH =
                ConfigStorage.SaveableConfig.fromConfig(
                        "width",
                        new ConfigInteger(
                                translate("width"), 280, 100, 600, translate("info.width")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> HEIGHT =
                ConfigStorage.SaveableConfig.fromConfig(
                        "height",
                        new ConfigInteger(
                                translate("height"), 117, 20, 400, translate("info.height")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> X =
                ConfigStorage.SaveableConfig.fromConfig(
                        "x", new ConfigInteger(translate("x"), 0, 0, 4000, translate("info.x")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> Y =
                ConfigStorage.SaveableConfig.fromConfig(
                        "y", new ConfigInteger(translate("y"), 30, 0, 4000, translate("info.y")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> MESSAGE_SPACE =
                ConfigStorage.SaveableConfig.fromConfig(
                        "messageSpace",
                        new ConfigInteger(
                                translate("messagespace"),
                                0,
                                0,
                                10,
                                translate("info.messagespace")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> LINE_SPACE =
                ConfigStorage.SaveableConfig.fromConfig(
                        "lineSpace",
                        new ConfigInteger(
                                translate("linespace"), 9, 8, 20, translate("info.linespace")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> LEFT_PAD =
                ConfigStorage.SaveableConfig.fromConfig(
                        "leftPad",
                        new ConfigInteger(
                                translate("leftpad"), 2, 0, 20, translate("info.leftpad")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> RIGHT_PAD =
                ConfigStorage.SaveableConfig.fromConfig(
                        "rightPad",
                        new ConfigInteger(
                                translate("rightpad"), 2, 0, 20, translate("info.rightpad")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> BOTTOM_PAD =
                ConfigStorage.SaveableConfig.fromConfig(
                        "bottomPad",
                        new ConfigInteger(
                                translate("bottompad"), 1, 0, 20, translate("info.bottompad")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> TOP_PAD =
                ConfigStorage.SaveableConfig.fromConfig(
                        "topPad",
                        new ConfigInteger(translate("toppad"), 0, 0, 20, translate("info.toppad")));

        public static final ConfigStorage.SaveableConfig<ConfigBoolean> SHOW_TABS =
                ConfigStorage.SaveableConfig.fromConfig(
                        "showTabs",
                        new ConfigBoolean(translate("showtabs"), true, translate("info.showtabs")));

        public static final ConfigStorage.SaveableConfig<ConfigOptionList> VISIBILITY =
                ConfigStorage.SaveableConfig.fromConfig(
                        "visibility",
                        new ConfigOptionList(
                                translate("visibility"),
                                Visibility.VANILLA,
                                translate("info.visibility")));

        public static final ConfigStorage.SaveableConfig<ConfigDouble> CHAT_SCALE =
                ConfigStorage.SaveableConfig.fromConfig(
                        translate("chatScale"),
                        new ConfigDouble(
                                translate("chatscale"), 1, 0, 1, translate("info.chatscale")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> FADE_TIME =
                ConfigStorage.SaveableConfig.fromConfig(
                        "fadeTime",
                        new ConfigInteger(
                                translate("fadetime"), 40, 0, 200, translate("info.fadetime")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> FADE_START =
                ConfigStorage.SaveableConfig.fromConfig(
                        "fadeStart",
                        new ConfigInteger(
                                translate("fadestart"),
                                100,
                                20,
                                1000,
                                translate("info.fadestart")));

        public static final ConfigStorage.SaveableConfig<ConfigOptionList> FADE_TYPE =
                ConfigStorage.SaveableConfig.fromConfig(
                        "fadeType",
                        new ConfigOptionList(
                                translate("fadetype"),
                                ConfigStorage.Easing.LINEAR,
                                translate("info.fadetype")));

        public static final ConfigStorage.SaveableConfig<ConfigSimpleColor> EMPTY_TEXT_COLOR =
                ConfigStorage.SaveableConfig.fromConfig(
                        "emptyTextColor",
                        new ConfigSimpleColor(
                                translate("emptytextcolor"),
                                ColorUtil.WHITE,
                                translate("info.emptytextcolor")));

        public static final ConfigStorage.SaveableConfig<ConfigOptionList> HUD_LINE_TYPE =
                ConfigStorage.SaveableConfig.fromConfig(
                        "hudLineType",
                        new ConfigOptionList(
                                translate("hudlinetype"),
                                HudLineType.FULL,
                                translate("info.hudlinetype")));

        public static final ConfigStorage.SaveableConfig<ConfigBoolean> ALTERNATE_LINES =
                ConfigStorage.SaveableConfig.fromConfig(
                        "alternateLines",
                        new ConfigBoolean(
                                translate("alternatelines"),
                                false,
                                translate("info.alternatelines")));

        public static final ConfigStorage.SaveableConfig<ConfigBoolean> SHOW_TIME =
                ConfigStorage.SaveableConfig.fromConfig(
                        "showTime",
                        new ConfigBoolean(
                                translate("showtime"), false, translate("info.showtime")));

        public static final ConfigStorage.SaveableConfig<ConfigInteger> STORED_LINES =
                ConfigStorage.SaveableConfig.fromConfig(
                        "storedLines",
                        new ConfigInteger(
                                translate("storedlines"),
                                200,
                                20,
                                1000,
                                translate("info.storedlines")));

        public static final ImmutableList<ConfigStorage.SaveableConfig<? extends IConfigBase>>
                OPTIONS =
                        ImmutableList.of(
                                CHAT_HEADS,
                                WIDTH,
                                HEIGHT,
                                X,
                                Y,
                                MESSAGE_SPACE,
                                LINE_SPACE,
                                LEFT_PAD,
                                RIGHT_PAD,
                                BOTTOM_PAD,
                                TOP_PAD,
                                SHOW_TABS,
                                VISIBILITY,
                                CHAT_SCALE,
                                FADE_TIME,
                                FADE_START,
                                FADE_TYPE,
                                EMPTY_TEXT_COLOR,
                                HUD_LINE_TYPE,
                                ALTERNATE_LINES,
                                SHOW_TIME,
                                STORED_LINES);
    }

    public static void loadFromFile() {
        File configFile =
                FileUtils.getConfigDirectory()
                        .toPath()
                        .resolve("advancedchat")
                        .resolve(CONFIG_FILE_NAME)
                        .toFile();

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = ConfigStorage.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                ChatTab.ChatTabJsonSave tabJson = new ChatTab.ChatTabJsonSave();
                JsonObject root = element.getAsJsonObject();
                JsonElement tab = root.get("maintab");
                if (tab.isJsonObject()) {
                    MAIN_TAB = tabJson.load(tab.getAsJsonObject());
                    AdvancedChatHud.MAIN_CHAT_TAB.refreshOptions();
                }
                JsonElement tabs = root.get("tabs");
                TABS.clear();
                if (tabs.isJsonArray()) {
                    for (JsonElement t : tabs.getAsJsonArray()) {
                        if (t.isJsonObject()) {
                            TABS.add(tabJson.load(t.getAsJsonObject()));
                        }
                    }
                }
                AdvancedChatHud.MAIN_CHAT_TAB.setUpTabs();

                JsonElement windows = root.get("windows");
                if (windows != null && windows.isJsonArray()) {
                    WindowManager.getInstance().loadFromJson(windows.getAsJsonArray());
                } else {
                    WindowManager.getInstance().loadFromJson(null);
                }

                ConfigStorage.readOptions(
                        root,
                        General.NAME,
                        (List<ConfigStorage.SaveableConfig<?>>) General.OPTIONS);

                int version = JsonUtils.getIntegerOrDefault(root, "configVersion", 0);
            }
        }
    }

    public static void saveFromFile() {
        File dir = FileUtils.getConfigDirectory().toPath().resolve("advancedchat").toFile();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            ChatTab.ChatTabJsonSave tabJson = new ChatTab.ChatTabJsonSave();
            JsonObject root = new JsonObject();

            ConfigStorage.writeOptions(
                    root, General.NAME, (List<ConfigStorage.SaveableConfig<?>>) General.OPTIONS);

            root.add("maintab", tabJson.save(MAIN_TAB));

            JsonArray tabs = new JsonArray();
            for (ChatTab tab : TABS) {
                tabs.add(tabJson.save(tab));
            }
            root.add("tabs", tabs);
            root.add("windows", WindowManager.getInstance().saveJson());
            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

            ConfigStorage.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveFromFile();
    }

    public enum HudLineType implements IConfigOptionListEntry {
        FULL("full"),
        COMPACT("compact");

        public final String configString;

        private static String translate(String key) {
            return StringUtils.translate("advancedchathud.config.hudlinetype." + key);
        }

        HudLineType(String configString) {
            this.configString = configString;
        }

        @Override
        public String getStringValue() {
            return configString;
        }

        @Override
        public String getDisplayName() {
            return translate(configString);
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int id = this.ordinal();
            if (forward) {
                id++;
            } else {
                id--;
            }
            if (id >= values().length) {
                id = 0;
            } else if (id < 0) {
                id = values().length - 1;
            }
            return values()[id % values().length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            return fromHudLineTypeString(value);
        }

        public static HudLineType fromHudLineTypeString(String hudlinetype) {
            for (HudLineType h : HudLineType.values()) {
                if (h.configString.equals(hudlinetype)) {
                    return h;
                }
            }
            return HudLineType.FULL;
        }
    }

    public enum Visibility implements IConfigOptionListEntry {
        VANILLA("vanilla"),
        ALWAYS("always"),
        FOCUSONLY("focus_only");

        private final String configString;

        @Getter private final Identifier texture;

        private static String translate(String key) {
            return StringUtils.translate("advancedchathud.config.visibility." + key);
        }

        Visibility(String configString) {
            this.texture =
                    new Identifier(
                            AdvancedChatHud.MOD_ID,
                            "textures/gui/chatwindow/" + configString + ".png");
            this.configString = configString;
        }

        @Override
        public String getStringValue() {
            return configString;
        }

        @Override
        public String getDisplayName() {
            return translate(configString);
        }

        @Override
        public Visibility cycle(boolean forward) {
            int id = this.ordinal();
            if (forward) {
                id++;
            } else {
                id--;
            }
            if (id >= values().length) {
                id = 0;
            } else if (id < 0) {
                id = values().length - 1;
            }
            return values()[id % values().length];
        }

        @Override
        public Visibility fromString(String value) {
            return fromVisibilityString(value);
        }

        public static Visibility fromVisibilityString(String visibility) {
            for (Visibility v : Visibility.values()) {
                if (v.configString.equals(visibility)) {
                    return v;
                }
            }
            return Visibility.VANILLA;
        }
    }
}
