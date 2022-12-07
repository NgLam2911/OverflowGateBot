package OverflowGateBot.command.commands.subcommands.BotCommands;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import OverflowGateBot.command.BotSubcommandClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildCommand extends BotSubcommandClass {
    public GuildCommand() {
        super("guild", "Hiển thị thông tin của máy chủ discord");
        this.addOption(OptionType.STRING, "guild", "Tên máy chủ", false, true);
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin của máy chủ discord mà bot đã gia nhập:\n\t<guild>: Tên máy chủ muốn xem, nếu không nhập guild thì sẽ hiện tất cả các máy chủ, ngược lại sẽ hiện thông tin máy chủ đã nhập";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildOption = event.getOption("guild");
        if (guildOption == null) {
            // Show all guild
            List<Guild> guilds = messagesHandler.jda.getGuilds();
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();
            for (Guild g : guilds) {
                String registered = guildHandler.guildIds.contains(g.getId()) ? "Đã được duyệt" : "Chưa được duyệt";
                field.append("_" + g.getName() + "_: " + registered + "\n");
            }
            builder.addField("_Máy chủ_", field.toString(), false);
            replyEmbeds(event, builder, 30);
        } else {
            // Get the guild base on name
            String guildName = guildOption.getAsString();
            List<Guild> guilds = messagesHandler.jda.getGuildsByName(guildOption.getAsString(), false);
            if (guilds.isEmpty())
                return;
            Guild firstGuild = guilds.get(0);
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();

            builder.setAuthor(guildName, null, firstGuild.getIconUrl());
            Member owner = firstGuild.getOwner();
            if (owner != null)
                field.append("```Chủ máy chủ: " + owner.getEffectiveName() + "\n");

            String status;
            if (guildHandler.guildIds.contains(firstGuild.getId())) {
                status = "Đã được duyệt";
                // TODO Extra info here
            } else {
                status = "Chưa được duyệt";
            }
            field.append("Số thành viên: " + firstGuild.getMemberCount() + "\n" + //
                    "Tình trạng: " + status + "\n" + //
                    "```");
            builder.setDescription("Link: " + firstGuild.getTextChannels().get(0).createInvite().complete().getUrl());
            builder.addField("Thông tin cơ bản:", field.toString(), false);
            replyEmbeds(event, builder, 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            Set<String> guildNames = new HashSet<String>();
            for (Guild g : messagesHandler.jda.getGuilds())
                guildNames.add(g.getName());
            sendAutoComplete(event, guildNames);
        }
    }
}
