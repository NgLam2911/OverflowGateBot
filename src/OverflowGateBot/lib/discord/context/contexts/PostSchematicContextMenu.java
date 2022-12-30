package OverflowGateBot.lib.discord.context.contexts;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;
import OverflowGateBot.main.MessageHandler;

public class PostSchematicContextMenu extends SimpleBotContextMenu {

    public PostSchematicContextMenu() { super("Post Schem"); }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendSchematicPreview(event.getTarget(), event.getTextChannel());
        event.getHook().deleteOriginal().queue();
    }
}
