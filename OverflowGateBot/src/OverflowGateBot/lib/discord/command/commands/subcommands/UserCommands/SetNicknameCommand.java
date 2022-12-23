package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class SetNicknameCommand extends BotSubcommandClass {
    public SetNicknameCommand() {
        super("setnickname", "Thay đổi tên của người dùng");
        this.addOption(OptionType.STRING, "nickname", "Biệt danh muốn đặt", true).//
                addOption(OptionType.USER, "user", "Tên người muốn đổi(Admin only");
    }

    @Override
    public String getHelpString() {
        return "Thay đổi tên của người dùng:\n\t<nickname>: Tên mới\n\t<user>: Tên người dùng muốn đổi tên (chỉ dành cho admin)";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("No guild found");

        Member bot = guild.getMember(jda.getSelfUser());
        Member target = event.getMember();
        OptionMapping userOption = event.getOption("user");
        OptionMapping nicknameOption = event.getOption("nickname");

        if (bot == null)
            throw new IllegalStateException("Bot not in guild");

        if (nicknameOption == null)
            throw new IllegalStateException("Invalid option");
        String nickname = nicknameOption.getAsString();

        if (userOption == null) {
            if (target == null)
                throw new IllegalStateException("Invalid option");
            else {
                if (!bot.canInteract(target))
                    return;
                target.modifyNickname(nickname).queue();
                reply(event, "Đổi biệt danh thành " + nickname, 10);
            }
        } else {
            if (userHandler.isAdmin(event.getMember())) {
                User user = userOption.getAsUser();
                target = guild.getMember(user);
                if (target == null)
                    throw new IllegalStateException("Bot not in guild");
                if (!bot.canInteract(target))
                    return;
                target.modifyNickname(nickname);
                reply(event, "Đổi biệt danh thành " + nickname, 10);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

        }
    }
}
