package OverflowGateBot.lib.discord.context;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class BotContextMenuClass {

    public String name;
    @Nonnull
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

    protected void replyEmbeds(MessageContextInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    protected void reply(MessageContextInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

}
