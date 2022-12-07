package OverflowGateBot.command.commands;


import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.subcommands.MindustryCommands.PingCommand;
import OverflowGateBot.command.commands.subcommands.MindustryCommands.PostMapCommand;
import OverflowGateBot.command.commands.subcommands.MindustryCommands.PostSchemCommand;
import OverflowGateBot.command.commands.subcommands.MindustryCommands.RefreshServerCommand;

public class MindustryCommand extends BotCommandClass {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostMapCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
    }
}
