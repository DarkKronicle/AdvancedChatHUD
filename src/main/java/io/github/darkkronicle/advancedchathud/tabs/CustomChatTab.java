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
import io.github.darkkronicle.Konstruct.type.BooleanObject;
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

    @Setter
    private Function function;

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
        setDefaultFunction();
    }

    public void setDefaultFunction() {
        function = new Function() {
            @Override
            public Result parse(ParseContext context, List<Node> input) {
                CustomChatTab self = ((ChatTabObject) Function.parseArgument(context, input, 0).getContent()).getTab();
                String search = Function.parseArgument(context, input, 1).getContent().getString();
                for (Match m : self.matches) {
                    if (SearchUtils.isMatch(search, m.getPattern(), m.getFindType())) {
                        return Result.success(new BooleanObject(true));
                    }
                }
                return Result.success(new BooleanObject(false));
            }

            @Override
            public IntRange getArgumentCount() {
                return IntRange.of(2);
            }
        };
    }

    @Override
    public boolean shouldAdd(Text text) {
        FluidText newText = new FluidText(text);
        String search = newText.getString();
        ParseContext context = AdvancedChatHud.MAIN_CHAT_TAB.getProcessor().createContext();
        Result result = function.parse(context, List.of(new Node() {
            @Override
            public Result parse(ParseContext context) {
                return Result.success(new ChatTabObject(CustomChatTab.this));
            }

            @Override
            public List<Node> getChildren() {
                return new ArrayList<>(0);
            }

            @Override
            public void addChild(Node node) {

            }
        }, new LiteralNode(search)));
        return result.getContent().getBoolean();
    }
}
