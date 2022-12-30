package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;
import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;

public class RefreshSlashCommand extends SimpleBotSubcommand {
    public RefreshSlashCommand() { super("refreshslashcommand", "Làm mới lại tất cả các lệnh trong máy chủ"); }

    @Override
    public String getHelpString() { return "Làm mới tất cả các lệnh trong máy chủ"; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        CommandHandler.unregisterCommand(event.getGuild());
        ContextMenuHandler.unregisterCommand(event.getGuild());
        CommandHandler.registerCommand(event.getGuild());
        ContextMenuHandler.registerCommand(event.getGuild());
        reply(event, "Đã làm mới lệnh", 30);
    }
}
