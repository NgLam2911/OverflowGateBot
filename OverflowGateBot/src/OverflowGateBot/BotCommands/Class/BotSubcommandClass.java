package OverflowGateBot.BotCommands.Class;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BotSubcommandClass extends SubcommandData {

    public BotSubcommandClass(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    // Override
    public String getHelpString() {
        return "";
    }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        runCommand(event);
    }

    // Override
    protected void runCommand(SlashCommandInteractionEvent event) {
    }

    // Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

    }

    public void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    public void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    // Auto complete handler
    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, Set<String> list) {
        if (list == null || list.isEmpty())
            return;
        List<Command.Choice> options = new ArrayList<Command.Choice>();
        String focusString = event.getFocusedOption().getValue().toLowerCase();

        for (String value : list) {
            if (value.toLowerCase().startsWith(focusString))
                options.add(new Command.Choice(value, value));
        }

        if (options.isEmpty()) {
            System.out.println("No options available for " + event.getFocusedOption().toString());
            return;
        }
        event.replyChoices(options).queue();
    }
}
