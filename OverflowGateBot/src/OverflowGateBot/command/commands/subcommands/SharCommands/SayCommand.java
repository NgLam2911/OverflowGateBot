package OverflowGateBot.command.commands.subcommands.SharCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashSet;
import java.util.List;

import OverflowGateBot.command.BotSubcommandClass;

import static OverflowGateBot.OverflowGateBot.*;

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
            List<Guild> guilds = jda.getGuildsByName(guildOption.getAsString(), false);
            if (guilds.isEmpty()) {
                event.getHook().deleteOriginal().queue();
                return;
            }
            Guild firstGuild = guilds.get(0);
            List<TextChannel> channels = firstGuild.getTextChannelsByName(channelOption.getAsString(), false);
            if (channels.isEmpty()) {
                event.getHook().deleteOriginal().queue();
                return;
            }
            TextChannel channel = channels.get(0);
            event.getHook().deleteOriginal().complete();
            channel.sendMessage(content).queue();

        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            HashSet<String> guildNames = new HashSet<>();
            jda.getGuilds().forEach(s -> guildNames.add(s.getName()));
            sendAutoComplete(event, guildNames);
        }

        // Show all channels
        else if (focus.equals("channel")) {
            // Get all channel form selected guild
            OptionMapping guildNameOption = event.getOption("guild");
            if (guildNameOption == null) {
                sendAutoComplete(event, "No guild name specified");
                return;
            }

            String guildName = guildNameOption.getAsString();
            List<Guild> guilds = jda.getGuildsByName(guildName, false);
            if (guilds.isEmpty()) {
                sendAutoComplete(event, "No guild found with name " + guildName);
                return;
            }

            Guild guild = guilds.get(0);
            List<TextChannel> channels = guild.getTextChannels();
            HashSet<String> channelNames = new HashSet<>();
            channels.forEach(c -> channelNames.add(c.getName()));
            sendAutoComplete(event, channelNames);

        }
    }
}
