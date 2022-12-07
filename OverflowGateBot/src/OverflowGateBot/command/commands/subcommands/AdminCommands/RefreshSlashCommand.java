package OverflowGateBot.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.commandHandler;
import static OverflowGateBot.OverflowGateBot.contextMenuHandler;

import OverflowGateBot.command.BotSubcommandClass;

public class RefreshSlashCommand extends BotSubcommandClass {
    public RefreshSlashCommand() {
        super("refreshslashcommand", "Làm mới lại tất cả các lệnh trong máy chủ");
    }

    @Override
    public String getHelpString() {
        return "Làm mới tất cả các lệnh trong máy chủ";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        commandHandler.unregisterCommand(event.getGuild());
        contextMenuHandler.unregisterCommand(event.getGuild());
        commandHandler.registerCommand(event.getGuild());
        contextMenuHandler.registerCommand(event.getGuild());
        reply(event, "Đã làm mới lệnh", 30);
    }
}


