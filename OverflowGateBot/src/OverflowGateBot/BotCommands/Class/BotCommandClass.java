package OverflowGateBot.BotCommands.Class;


import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class BotCommandClass {
    public String name;
    public String description;

    @Nonnull
    public SlashCommandData command;
    public HashMap<String, BotSubcommandClass> subcommands = new HashMap<>();

    public BotCommandClass(@Nonnull String name, @Nonnull String description) {
        this.name = name;
        this.description = description;
        command = Commands.slash(name, description);
    }

    // Override
    public String getHelpString() {
        return "";
    }

    // Can be overridden
    public String getHelpString(String subCommand) {
        if (!subcommands.containsKey(subCommand))
            return "Không tìm thấy lệnh " + subCommand;
        return subcommands.get(subCommand).getHelpString();
    }

    // Can be overridden
    public void onCommand(SlashCommandInteractionEvent event) {
        runCommand(event);
    }

    protected void runCommand(SlashCommandInteractionEvent event) {
        if (subcommands.containsKey(event.getSubcommandName()))
            subcommands.get(event.getSubcommandName()).onCommand(event);
        else
            reply(event, "Lệnh sai rồi kìa", 10);
    }

    public BotSubcommandClass addSubcommands(BotSubcommandClass subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
        command.addSubcommands(subcommand);
        return subcommand;
    }

    // Can be overridden
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }

    protected void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    protected void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

}
