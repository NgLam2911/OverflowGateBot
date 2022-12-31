package OverflowGateBot.lib.discord.command.commands.subcommands.GameCommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;
import OverflowGateBot.lib.inf.Updatable;
import OverflowGateBot.lib.minigame.tictactoe.TicTacToe;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TicTacToeCommand extends SimpleBotSubcommand implements Updatable {

    private static List<TicTacToe> cache = new ArrayList<TicTacToe>();

    public TicTacToeCommand() { super("tictactoe", "Cờ ca rô"); }

    @Override
    public String getHelpString() { return "Cờ ca rô"; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        TicTacToe game = new TicTacToe(event);
        game.init();
        cache.add(game);
    }

    public void update() {
        Iterator<TicTacToe> iterator = cache.iterator();
        TicTacToe game;
        while (iterator.hasNext()) {
            game = iterator.next();
            if (!game.isAlive(1)) {
                game.delete();
                iterator.remove();
            }
        }
    }

}
