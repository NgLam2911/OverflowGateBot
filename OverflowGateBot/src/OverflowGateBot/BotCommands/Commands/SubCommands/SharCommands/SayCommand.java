package OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.messagesHandler;
import static OverflowGateBot.OverflowGateBot.guildHandler;

import java.util.HashMap;
import java.util.List;

public class SayCommand extends BotSubcommandClass {

    public SayCommand() {
        super("say", "Shar only");
        this.addOption(OptionType.STRING, "content", "Shar only", true).//
                addOption(OptionType.STRING, "guild", "Shar only", false, true).//
                addOption(OptionType.STRING, "channel", "Shar only", false, true);
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildOption = event.getOption("guild");
        OptionMapping channelOption = event.getOption("channel");
        OptionMapping contentOption = event.getOption("content");
        if (contentOption == null)
            return;
        String content = contentOption.getAsString();

        if (guildOption == null && channelOption == null) {
            event.getTextChannel().sendMessage(content).queue();
            event.getHook().deleteOriginal().queue();

        } else if (guildOption != null && channelOption != null) {
            List<Guild> guilds = messagesHandler.jda.getGuildsByName(guildOption.getAsString(), false);
            if (guilds.isEmpty())
                return;
            Guild firstGuild = guilds.get(0);
            List<TextChannel> channels = firstGuild.getTextChannelsByName(channelOption.getAsString(), false);
            if (channels.isEmpty())
                return;
            TextChannel channel = channels.get(0);
            channel.sendMessage(content).queue();
            event.getHook().deleteOriginal().queue();

        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild"))
            sendAutoComplete(event, guildHandler.getGuildsName().keySet());

        // Show all channels
        else if (focus.equals("channel")) {
            // Get all channel form selected guild
            OptionMapping guildIdOption = event.getOption("guild");
            if (guildIdOption == null)
                return;
            HashMap<String, String> guilds = guildHandler.getGuildsName();
            String guildName = guildIdOption.getAsString();

            if (!guilds.containsKey(guildName))
                return;

            String guildId = guilds.get(guildName);
            if (guildId == null) {
                System.out.println("Not found guild " + guildName);
                return;
            }
            sendAutoComplete(event, guildHandler.getChannelsName(guildId).keySet());

        }
    }
}
