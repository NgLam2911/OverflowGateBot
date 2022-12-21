package OverflowGateBot.lib.discord.command.commands;


import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PingCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PostMapCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.PostSchemCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands.RefreshServerCommand;

public class MindustryCommand extends BotCommandClass {
    public MindustryCommand() {
        super("mindustry", "Các lệnh liên quan đến mindustry");
        addSubcommands(new PingCommand());
        addSubcommands(new PostMapCommand());
        addSubcommands(new PostSchemCommand());
        addSubcommands(new RefreshServerCommand());
    }
}
