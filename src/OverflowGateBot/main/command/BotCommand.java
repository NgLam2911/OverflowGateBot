package OverflowGateBot.main.command;


import OverflowGateBot.main.command.subcommands.BotCommands.GuildCommand;
import OverflowGateBot.main.command.subcommands.BotCommands.HelpCommand;
import OverflowGateBot.main.command.subcommands.BotCommands.InfoCommand;
import OverflowGateBot.main.util.SimpleBotCommand;

public class BotCommand extends SimpleBotCommand {
    public BotCommand() {
        super("bot", "Các lệnh liên quan đến bot");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
    }

}
