/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.tabs;

import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import java.util.UUID;
import lombok.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

/** Base ChatTab that allows for custom chat tabs in AdvancedChatHud. */
@Environment(EnvType.CLIENT)
@Data
public abstract class AbstractChatTab {

    // Each tab stores their own messages.
    protected String name;
    protected String abreviation;
    protected ColorUtil.SimpleColor mainColor;
    protected ColorUtil.SimpleColor borderColor;
    protected ColorUtil.SimpleColor innerColor;
    protected UUID uuid;
    private int unread = 0;
    protected boolean showUnread;

    public AbstractChatTab(
            String name,
            String abreviation,
            ColorUtil.SimpleColor mainColor,
            ColorUtil.SimpleColor borderColor,
            ColorUtil.SimpleColor innerColor,
            boolean showUnread,
            UUID uuid) {
        this.name = name;
        this.abreviation = abreviation;
        this.mainColor = mainColor;
        this.showUnread = showUnread;
        this.innerColor = innerColor;
        this.borderColor = borderColor;
        this.uuid = uuid;
    }

    public void addNewUnread() {
        this.unread++;
    }

    public void resetUnread() {
        this.unread = 0;
    }

    /**
     * If the inputted message should be put into the chat tab.
     *
     * @param text Object to search.
     * @return True if it should be added.
     */
    public abstract boolean shouldAdd(Text text);
}
