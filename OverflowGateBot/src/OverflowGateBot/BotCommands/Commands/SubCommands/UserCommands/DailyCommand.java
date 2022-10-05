package OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.userHandler;

public class DailyCommand extends BotSubcommandClass {
    public DailyCommand() {
        super("daily", "Điểm danh");
    }

    @Override
    public String getHelpString() {
        return "Điểm danh";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        int money = userHandler.getDaily(event.getMember());
        if (money > 0)
            reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + "MM", 30);
        else
            reply(event, "Bạn đã điểm danh hôm nay", 10);
    }
}
