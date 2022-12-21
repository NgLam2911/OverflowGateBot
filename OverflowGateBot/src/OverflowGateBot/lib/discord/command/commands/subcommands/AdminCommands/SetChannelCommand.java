package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.guildHandler;

import java.util.HashMap;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class SetChannelCommand extends BotSubcommandClass {

    public SetChannelCommand() {
        super("setchannel", "Cài đặt các kênh của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại kênh muốn đặt", true, true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các kênh của máy chủ:\n\t<type>: loại kênh muốn đặt";
    }

    @Override
    @SuppressWarnings("unchecked")
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

            if (!guildHandler.guildChannels.contains(type)) {
                reply(event, "Không tìm thấy kênh " + type, 10);
                return;

            } else {
                if (guildHandler.guildConfigs.get(channel.getGuild().getId()) == null)
                    return;
                HashMap<String, String> channels = (HashMap<String, String>) guildHandler.guildConfigs
                        .get(channel.getGuild().getId()).get(type);
                if (channels != null && !channels.containsKey(channel.getId())) {
                    ((HashMap<String, String>) guildHandler.guildConfigs.get(channel.getGuild().getId()).get(type))
                            .put(channel.getId(), null);
                    reply(event, "Đã thêm " + channel.getName() + " vào danh sách kênh " + type, 10);
                } else if (channels != null && channels.containsKey(channel.getId())) {
                    ((HashMap<String, String>) guildHandler.guildConfigs.get(channel.getGuild().getId()).get(type))
                            .remove(channel.getId());
                    reply(event, "Đã xóa " + channel.getName() + " khỏi danh sách kênh " + type, 10);

                }
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
            guildHandler.guildChannels.forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);
        }
    }
}
