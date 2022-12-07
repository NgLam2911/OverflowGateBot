package OverflowGateBot.command.commands;


import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.subcommands.BotCommands.GuildCommand;
import OverflowGateBot.command.commands.subcommands.BotCommands.HelpCommand;
import OverflowGateBot.command.commands.subcommands.BotCommands.InfoCommand;

public class BotCommand extends BotCommandClass {
    public BotCommand() {
        super("bot", "Các lệnh liên quan đến bot");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
    }

}
