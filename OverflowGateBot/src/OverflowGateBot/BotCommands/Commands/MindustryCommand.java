package OverflowGateBot.BotCommands.Commands;


import OverflowGateBot.BotCommands.Class.BotCommandClass;
import OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands.PingCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands.PostMapCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands.RefreshServerCommand;

public class MindustryCommand extends BotCommandClass {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostMapCommand());
        addSubcommands(new PostMapCommand());
        addSubcommands(new RefreshServerCommand());
    }
}
