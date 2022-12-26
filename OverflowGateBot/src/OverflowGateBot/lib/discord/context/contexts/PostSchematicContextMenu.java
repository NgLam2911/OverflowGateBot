package OverflowGateBot.lib.discord.context.contexts;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;

public class PostSchematicContextMenu extends SimpleBotContextMenu {

    public PostSchematicContextMenu() {
        super("Post Schem");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        messagesHandler.sendSchematicPreview(event.getTarget(), event.getTextChannel());
    }
}
