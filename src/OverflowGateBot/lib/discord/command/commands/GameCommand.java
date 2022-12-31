package OverflowGateBot.lib.discord.command.commands;

import OverflowGateBot.lib.discord.command.SimpleBotCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.GameCommands.TicTacToeCommand;

public class GameCommand extends SimpleBotCommand {
    public GameCommand() {
        super("game", "Các lệnh liên quan đến mini game");
        addSubcommands(new TicTacToeCommand());
    }

}
