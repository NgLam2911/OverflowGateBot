package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;

import static OverflowGateBot.OverflowGateBot.userHandler;

public class InfoCommand extends BotSubcommandClass {
    public InfoCommand() {
        super("info", "Hiển thị thông tin người dùng");
        this.addOption(OptionType.USER, "user", "Tên thành viên", false);
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin người dùng:\n\t<user>: Tên người dùng muốn xem thông tin, nếu không nhập thì hiển thị thông tin bản thân";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");

        // Display command caller information
        if (userOption == null) {
            Member member = event.getMember();
            if (member == null) {
                reply(event, "Lỗi không xác định", 10);
                return;
            }
            replyEmbeds(event, getDisplayedUserInformation(member), 30);

        } else {
            // Display target information
            User user = userOption.getAsUser();
            Guild guild = event.getGuild();
            if (guild == null) {
                reply(event, "Lỗi không xác định", 10);
                return;
            }
            Member member = guild.getMember(user);
            if (member == null) {
                reply(event, "Người dùng với tên " + user.getName() + " không thuộc máy chủ", 10);
                return;
            }
            replyEmbeds(event, getDisplayedUserInformation(member), 30);
        }
    }

    private EmbedBuilder getDisplayedUserInformation(@Nonnull Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
        UserData user = userHandler.getUserAwait(member).data;
        builder.addField("Thông tin cơ bản",
                "Cấp: " + user.level + " (" + user.point + "\\" + user._getLevelCap() + ")" + //
                        "\nTổng kinh nghiệm: " + user._getTotalPoint(),
                false);
        builder.addField("Điểm", "Tổng điểm cống hiến: " + user.money + //
                "\nTổng điểm pvp: " + user.pvpPoint, false);

        return builder;
    }

}
