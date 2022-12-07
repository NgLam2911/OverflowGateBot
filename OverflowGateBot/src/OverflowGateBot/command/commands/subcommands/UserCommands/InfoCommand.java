package OverflowGateBot.command.commands.subcommands.UserCommands;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;

import OverflowGateBot.command.BotSubcommandClass;

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
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        if (event.getOption("user") == null) {
            replyEmbeds(event, userHandler.getUserInfo(event.getMember()), 30);
        } else {
            OptionMapping userOption = event.getOption("user");
            if (userOption == null)
                return;
            User user = userOption.getAsUser();

            replyEmbeds(event, userHandler.getUserInfo(guild.getMember(user)), 30);
        }
    }

}
