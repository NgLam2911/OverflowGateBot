package OverflowGateBot.BotCommands.Commands;


import OverflowGateBot.BotCommands.Class.BotCommandClass;
import OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands.GuildCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands.HelpCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands.InfoCommand;

public class BotCommand extends BotCommandClass {
    public BotCommand() {
        super("bot", "Các lệnh liên quan đến bot");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
    }

}
