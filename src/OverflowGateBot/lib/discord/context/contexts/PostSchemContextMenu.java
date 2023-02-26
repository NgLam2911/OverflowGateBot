package OverflowGateBot.lib.discord.context.contexts;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;
import OverflowGateBot.main.MessageHandler;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class PostSchemContextMenu extends SimpleBotContextMenu {
    public PostSchemContextMenu() {
        super("Post Schematic");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendSchematicPreview(event.getTarget());
        event.getHook().deleteOriginal().queue();
    }
}
