package OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands;

import java.util.HashMap;
import java.util.List;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
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

    // TODO

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping guildOption = event.getOption("guild");
        if (guildOption == null) {
            // Show all guild
            List<Guild> guilds = jda.getGuilds();
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();
            builder.addField("_Máy chủ_", field.toString(), false);
            replyEmbeds(event, builder, 30);
        } else {
            // Get the guild base on name
            String guildId = guildOption.getAsString();
            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                return;

            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();

            builder.setAuthor(guild.getName(), null, guild.getIconUrl());
            Member owner = guild.getOwner();
            if (owner != null)
                field.append("```Chủ máy chủ: " + owner.getEffectiveName() + "\n");

            String status;
            field.append("Số thành viên: " + guild.getMemberCount() + "\n" + //
                    "```");
            builder.setDescription("Link: " + guild.getTextChannels().get(0).createInvite().complete().getUrl());
            builder.addField("Thông tin cơ bản:", field.toString(), false);
            replyEmbeds(event, builder, 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("guild")) {
            HashMap<String, String> options = new HashMap<String, String>();
            jda.getGuilds().forEach(t -> options.put(t.getName(), t.getId()));
            sendAutoComplete(event, options);

        }
    }
}
