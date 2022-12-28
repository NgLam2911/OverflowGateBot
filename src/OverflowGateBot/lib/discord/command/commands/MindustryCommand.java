package OverflowGateBot.lib.discord.command.commands;

import OverflowGateBot.lib.discord.command.SimpleBotCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PingCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PostMapCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PostSchemCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.RefreshServerCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.SearchSchematicCommand;

public class MindustryCommand extends SimpleBotCommand {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostMapCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
        addSubcommands(new SearchSchematicCommand());
    }
}
