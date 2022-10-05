package OverflowGateBot.BotCommands.Commands.SubCommands.AdminCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.commandHandler;;

public class RefreshSlashCommand extends BotSubcommandClass {
    public RefreshSlashCommand() {
        super("refreshslash", "Làm mới lại tất cả các lệnh trong máy chủ");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        commandHandler.unregisterCommand(event.getGuild());
        commandHandler.registerCommand(event.getGuild());
        reply(event, "Đã làm mới lệnh", 30);
    }
}

