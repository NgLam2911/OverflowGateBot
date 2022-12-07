package OverflowGateBot.context;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class BotContextMenuClass{

    public String name;
    public CommandData command;

    public BotContextMenuClass(@Nonnull String name) {
        this.name = name;
        command = Commands.message(name);
    }

    public void onCommand(MessageContextInteractionEvent event) {
        runCommand(event);
    }

    protected void runCommand(MessageContextInteractionEvent event) {
    }

}
