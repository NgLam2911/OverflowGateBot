package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.userHandler;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class DailyCommand extends BotSubcommandClass {
    public DailyCommand() {
        super("daily", "Điểm danh");
    }

    @Override
    public String getHelpString() {
        return "Điểm danh mỗi ngày";
    }
    // TODO
    /*
     * @Override
     * public void onCommand(SlashCommandInteractionEvent event) {
     * int money = userHandler.getDaily(event.getMember());
     * if (money > 0)
     * reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + "MM", 30);
     * else
     * reply(event, "Bạn đã điểm danh hôm nay", 10);
     * }
     */
}
