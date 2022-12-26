package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.commandHandler;
import static OverflowGateBot.OverflowGateBot.contextMenuHandler;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;

public class RefreshSlashCommand extends SimpleBotSubcommand {
    public RefreshSlashCommand() {
        super("refreshslashcommand", "Làm mới lại tất cả các lệnh trong máy chủ");
    }

    @Override
    public String getHelpString() {
        return "Làm mới tất cả các lệnh trong máy chủ";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        contextMenuHandler.unregisterCommand(event.getGuild());
        commandHandler.registerCommand(event.getGuild());
        contextMenuHandler.registerCommand(event.getGuild());
        reply(event, "Đã làm mới lệnh", 30);
    }
}


