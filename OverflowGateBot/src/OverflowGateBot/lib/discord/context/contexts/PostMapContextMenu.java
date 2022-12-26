package OverflowGateBot.lib.discord.context.contexts;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;

public class PostMapContextMenu extends SimpleBotContextMenu {

    public PostMapContextMenu() {
        super("Post Map");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        messagesHandler.sendMapPreview(event.getTarget(), event.getTextChannel());
    }
}
