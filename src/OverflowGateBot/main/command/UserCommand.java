package OverflowGateBot.main.command;

import OverflowGateBot.main.command.subcommands.UserCommands.AvatarCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.DailyCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.DeletedMessageCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.InfoCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.LeaderboardCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.SetNicknameCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.ShowLevelCommand;
import OverflowGateBot.main.command.subcommands.UserCommands.TransferCommand;
import OverflowGateBot.main.util.SimpleBotCommand;

public class UserCommand extends SimpleBotCommand {
    public UserCommand() {
        super("user", "Lệnh liên quan đến người dùng");
        addSubcommands(new DailyCommand());
        addSubcommands(new ShowLevelCommand());
        addSubcommands(new InfoCommand());
        addSubcommands(new LeaderboardCommand());
        addSubcommands(new SetNicknameCommand());
        addSubcommands(new TransferCommand());
        addSubcommands(new AvatarCommand());
        addSubcommands(new DeletedMessageCommand());
    }

}
