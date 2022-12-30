package OverflowGateBot.lib.discord.context.contexts;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;
import OverflowGateBot.main.MessageHandler;

public class PostMapContextMenu extends SimpleBotContextMenu {

    public PostMapContextMenu() { super("Post Map"); }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        MessageHandler.sendMapPreview(event.getTarget(), event.getTextChannel());
        event.getHook().deleteOriginal().queue();
    }
}
