/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.tabs;

import io.github.darkkronicle.Konstruct.NodeException;
import io.github.darkkronicle.Konstruct.functions.Function;
import io.github.darkkronicle.Konstruct.functions.Variable;
import io.github.darkkronicle.Konstruct.nodes.LiteralNode;
import io.github.darkkronicle.Konstruct.nodes.Node;
import io.github.darkkronicle.Konstruct.parser.*;
import io.github.darkkronicle.advancedchatcore.konstruct.AdvancedChatKonstruct;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchathud.AdvancedChatHud;
import io.github.darkkronicle.advancedchathud.config.ChatTab;
import io.github.darkkronicle.advancedchathud.config.Match;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;

/** ChatTab that loads from {@link ChatTab}. Easy to customize. */
public class CustomChatTab extends AbstractChatTab {

    @Getter private List<Match> matches;

    @Setter
    @Getter
    private boolean forward;

    @Getter
    @Setter
    private String startingMessage;

    @Getter
    @Setter
    private ChatTab tab;

    private NodeProcessor processor = null;
    private Function function = null;

    @Getter
    @Setter
    private boolean konstructed = false;

    public CustomChatTab(ChatTab tab) {
        super(
            tab.getName().config.getStringValue(),
            tab.getAbbreviation().config.getStringValue(),
            tab.getMainColor().config.get(),
            tab.getBorderColor().config.get(),
            tab.getInnerColor().config.get(),
            tab.getShowUnread().config.getBooleanValue(),
            tab.getUuid()
        );
        this.tab = tab;
        this.matches = new ArrayList<>(tab.getMatches());
        this.forward = tab.getForward().config.getBooleanValue();
        this.startingMessage = tab.getStartingMessage().config.getStringValue();
    }

    public void setNode(String content) {
        konstructed = true;
        ParseResult result;
        try {
            Node node = AdvancedChatKonstruct.getInstance().getNode(content);
            processor = AdvancedChatKonstruct.getInstance().copy();
            processor.addVariable("tab", Variable.of(new ChatTabObject(this)));
            result = processor.parse(node);
        } catch (NodeException e) {
            AdvancedChatHud.LOGGER.log(Level.ERROR, "Error setting up konstruct chat tab " + tab.getName(), e);
            return;
        }
        Optional<Function> func = result.getContext().getFunction("shouldAdd");
        if (func.isEmpty()) {
            AdvancedChatHud.LOGGER.log(Level.ERROR, "Error setting up konstruct chat tab " + tab.getName() + ". Function 'shouldAdd(text)' does not exist!");
            return;
        }
        if (!func.get().getArgumentCount().equals(IntRange.of(1))) {
            AdvancedChatHud.LOGGER.log(Level.ERROR, "Error setting up konstruct chat tab " + tab.getName() + ". Function 'shouldAdd(text)' should only have one argument!");
        }
        function = func.get();
    }

    @Override
    public boolean shouldAdd(Text text) {
        if (konstructed) {
            if (function == null) {
                return false;
            }
            ParseContext context = processor.createContext();
            Result result = function.parse(context, List.of(new LiteralNode(text.getString())));
            return result.getContent().getBoolean();
        }
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
