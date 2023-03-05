package OverflowGateBot.main.command;

import OverflowGateBot.main.command.subcommands.MindustryCommands.PingCommand;
import OverflowGateBot.main.command.subcommands.MindustryCommands.PostSchemCommand;
import OverflowGateBot.main.command.subcommands.MindustryCommands.RefreshServerCommand;
import OverflowGateBot.main.command.subcommands.MindustryCommands.SearchSchematicCommand;
import OverflowGateBot.main.util.SimpleBotCommand;

public class MindustryCommand extends SimpleBotCommand {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
    }
}
