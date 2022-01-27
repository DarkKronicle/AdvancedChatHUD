/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud;

import fi.dy.masa.malilib.event.InitializationHandler;
import io.github.darkkronicle.advancedchatcore.ModuleHandler;
import io.github.darkkronicle.advancedchathud.tabs.MainChatTab;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class AdvancedChatHud implements ClientModInitializer {

    public static final String MOD_ID = "advancedchathud";
    public static MainChatTab MAIN_CHAT_TAB;
    public static Logger LOGGER = LogManager.getLogger("AdvancedChatHUD");

    @Override
    public void onInitializeClient() {
        // This will run after AdvancedChatCore's because of load order
        ModuleHandler.getInstance().registerInitHandler(MOD_ID, 0, new HudInitHandler());
    }
}
