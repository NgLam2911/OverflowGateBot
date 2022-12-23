package OverflowGateBot.lib.discord.command.commands;

import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.AvatarCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.DailyCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.HideLevelCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.InfoCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.LeaderboardCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.SetNicknameCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.TransferCommand;

public class UserCommand extends BotCommandClass {
    public UserCommand() {
        super("user", "Lệnh liên quan đến người dùng");
        addSubcommands(new DailyCommand());
        addSubcommands(new HideLevelCommand());
        addSubcommands(new InfoCommand());
        addSubcommands(new LeaderboardCommand());
        addSubcommands(new SetNicknameCommand());
        addSubcommands(new TransferCommand());
        addSubcommands(new AvatarCommand());
    }

}