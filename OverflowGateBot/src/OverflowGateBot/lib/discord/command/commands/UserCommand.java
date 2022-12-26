package OverflowGateBot.lib.discord.command.commands;

import OverflowGateBot.lib.discord.command.SimpleBotCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.AvatarCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.DailyCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.DeletedMessageCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.ShowLevelCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.InfoCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.LeaderboardCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.SetNicknameCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands.TransferCommand;

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
