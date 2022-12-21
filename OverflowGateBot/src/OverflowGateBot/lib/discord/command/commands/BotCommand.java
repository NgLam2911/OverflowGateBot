package OverflowGateBot.lib.discord.command.commands;


import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands.GuildCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands.HelpCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands.InfoCommand;

public class BotCommand extends BotCommandClass {
    public BotCommand() {
        super("bot", "Các lệnh liên quan đến bot");
        addSubcommands(new InfoCommand());
        addSubcommands(new GuildCommand());
        addSubcommands(new HelpCommand());
    }

}
