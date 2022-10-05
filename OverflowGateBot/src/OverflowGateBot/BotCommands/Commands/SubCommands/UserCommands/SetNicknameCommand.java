package OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

public class SetNicknameCommand extends BotSubcommandClass {
    public SetNicknameCommand() {
        super("setnickname", "Thay đổi tên của người dùng");
        this.addOption(OptionType.STRING, "nickname", "Biệt danh muốn đặt", true).//
                addOption(OptionType.USER, "user", "Tên người muốn đổi(Admin only");
    }

    @Override
    public String getHelpString() {
        return "Thay đổi tên của người dùng";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        OptionMapping userOption = event.getOption("user");
        OptionMapping nicknameOption = event.getOption("nickname");
        if (nicknameOption == null)
            return;
        if (userOption == null) {
            userHandler.setNickName(event.getMember(), nicknameOption.getAsString());
            reply(event, "Đổi biệt danh thành " + nicknameOption.getAsString(), 10);
        } else {
            if (guildHandler.isAdmin(event.getMember())) {
                User user = userOption.getAsUser();
                userHandler.setNickName(guild.getMember(user), nicknameOption.getAsString());
                reply(event, "Đổi biệt danh thành " + nicknameOption.getAsString(), 10);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
        }
    }
}
