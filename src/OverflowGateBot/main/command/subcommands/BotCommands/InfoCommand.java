package OverflowGateBot.main.command.subcommands.BotCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import OverflowGateBot.BotConfig;
import OverflowGateBot.main.handler.GuildHandler;
import OverflowGateBot.main.handler.UserHandler;
import OverflowGateBot.main.util.SimpleBotSubcommand;

import static OverflowGateBot.OverflowGateBot.*;

import java.time.format.DateTimeFormatter;

public class InfoCommand extends SimpleBotSubcommand {
    public InfoCommand() {
        super("info", "Hiển thị thông tin cơ bản của bot");
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin cơ bản của bot";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        Member shar = guild.getMemberById(BotConfig.SHAR_ID);

        Long totalMember = 0l;
        User bot = jda.getSelfUser();

        for (Guild g : jda.getGuilds())
            totalMember += g.getMemberCount();

        if (shar == null)
            field.append("Chủ nhân: Sharlotte\n");
        else
            field.append("Chủ nhân: " + shar.getEffectiveName() + "\n");

        field.append("Máy chủ: " + GuildHandler.getActiveGuildCount() + "\\" + jda.getGuilds().size() + " trực tuyến\n");
        field.append("Thành viên: " + UserHandler.getActiveUserCount() + "\\" + totalMember + " hoạt động\n");
        field.append("Ngày sinh: " + bot.getTimeCreated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy-hh:mm:ss"))
                + "\n");
        builder.addField("Thông tin", "```" + field.toString() + "```", false);
        builder.setThumbnail(bot.getEffectiveAvatarUrl());
        builder.setTitle(bot.getName(), jda.getInviteUrl(Permission.ADMINISTRATOR));

        replyEmbed(event, builder, 30);
    }
}
