package OverflowGateBot.main.command.subcommands.UserCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.annotation.Nonnull;

import OverflowGateBot.main.handler.UserHandler;
import OverflowGateBot.main.user.UserData;
import OverflowGateBot.main.util.SimpleBotSubcommand;

import java.util.List;

public class InfoCommand extends SimpleBotSubcommand {
    public InfoCommand() {
        super("info", "Hiển thị thông tin người dùng", true, false);
        this.addOption(OptionType.USER, "user", "Tên thành viên", false);
    }

    @Override
    public String getHelpString() { return "Hiển thị thông tin người dùng:\n\t<user>: Tên người dùng muốn xem thông tin, nếu không nhập thì hiển thị thông tin bản thân"; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");

        // Display command caller information
        if (userOption == null) {
            Member member = event.getMember();
            if (member == null) {
                reply(event, "Lỗi không xác định", 10);
                return;
            }

            replyEmbed(event, getDisplayedUserInformation(member), 30);

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
            replyEmbed(event, getDisplayedUserInformation(member), 30);
        }
    }

    private EmbedBuilder getDisplayedUserInformation(@Nonnull Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
        // Display role
        List<Role> roles = member.getRoles();
        String roleString = "";
        for (Role role : roles)
            roleString += role.getName() + ", ";
        if (!roleString.isEmpty())
            roleString = roleString.substring(0, roleString.length() - 2);
        // Display point

        UserData user = UserHandler.getUserNoCache(member);
        user._displayLevelName();

        builder.addField("Vai trò", roleString, false);
        builder.addField("Thông tin cơ bản", "Cấp: " + user.level + " (" + user.point + "\\" + user._getLevelCap() + ")" + //
                "\nTổng kinh nghiệm: " + user._getTotalPoint(), false);
        builder.addField("Điểm", "Tổng điểm cống hiến: " + user.money + //
                "\nTổng điểm pvp: " + user.pvpPoint, false);

        return builder;
    }

}
