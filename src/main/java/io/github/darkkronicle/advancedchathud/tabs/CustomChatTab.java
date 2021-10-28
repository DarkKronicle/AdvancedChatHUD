/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.tabs;

import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.Match;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.minecraft.text.Text;

/** ChatTab that loads from {@link ChatTab}. Easy to customize. */
public class CustomChatTab extends AbstractChatTab {

    @Getter private String name;

    @Getter private List<Match> matches;

    @Getter private boolean forward;

    @Getter private String startingMessage;

    @Getter private ChatTab storage;

    public CustomChatTab(ChatTab storage) {
        super(
                storage.getName().config.getStringValue(),
                storage.getAbbreviation().config.getStringValue(),
                storage.getMainColor().config.getSimpleColor(),
                storage.getBorderColor().config.getSimpleColor(),
                storage.getInnerColor().config.getSimpleColor(),
                storage.getShowUnread().config.getBooleanValue(),
                storage.getUuid());
        this.storage = storage;
        this.matches = new ArrayList<>(storage.getMatches());
        this.forward = storage.getForward().config.getBooleanValue();
        this.startingMessage = storage.getStartingMessage().config.getStringValue();
    }

    @Override
    public boolean shouldAdd(Text text) {
        FluidText newText = new FluidText(text);
        String search = newText.getString();
        for (Match m : matches) {
            if (SearchUtils.isMatch(search, m.getPattern(), m.getFindType())) {
                return true;
            }
        }
        return false;
    }
}
