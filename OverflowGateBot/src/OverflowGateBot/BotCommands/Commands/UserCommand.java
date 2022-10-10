package OverflowGateBot.BotCommands.Commands;

import OverflowGateBot.BotCommands.Class.BotCommandClass;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.AvatarCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.DailyCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.GuessTheNumberCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.HideLevelCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.InfoCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.LeaderboardCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.SetNicknameCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands.TransferCommand;

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
        addSubcommands(new GuessTheNumberCommand());
    }

}
