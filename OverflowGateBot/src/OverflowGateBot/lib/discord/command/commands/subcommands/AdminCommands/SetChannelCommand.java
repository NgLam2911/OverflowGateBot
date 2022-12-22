package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.guildHandler;

import java.util.HashMap;

import OverflowGateBot.lib.data.GuildData.CHANNEL_TYPE;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.GuildHandler.GuildCache;

public class SetChannelCommand extends BotSubcommandClass {

    public SetChannelCommand() {
        super("setchannel", "Cài đặt các kênh của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại kênh muốn đặt", true, true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các kênh của máy chủ:\n\t<type>: loại kênh muốn đặt\n\tThêm lần nữa để xóa";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null)
            return;

        String type = typeOption.getAsString();
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        try {
            TextChannel channel = event.getTextChannel();
            GuildCache guildData = guildHandler.getGuild(guild);
            if (guildData == null)
                throw new IllegalStateException("No guild data found");

            if (guildData.data._containsChannel(type, channel.getId())) {
                guildData.data._removeChannel(type, channel.getId());
            } else {
                guildData.data._addChannel(type, channel.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (CHANNEL_TYPE t : CHANNEL_TYPE.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);
        }
    }

}
