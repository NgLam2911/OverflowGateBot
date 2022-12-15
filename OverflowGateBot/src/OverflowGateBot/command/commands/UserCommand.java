package OverflowGateBot.command.commands;

import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.subcommands.UserCommands.AvatarCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.DailyCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.HideLevelCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.InfoCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.LeaderboardCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.SetNicknameCommand;
import OverflowGateBot.command.commands.subcommands.UserCommands.TransferCommand;

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
