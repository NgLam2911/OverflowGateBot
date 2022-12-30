package OverflowGateBot.lib.minigame.tictactoe;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.table.SimpleTable;
import OverflowGateBot.lib.user.UserData;

import java.awt.*;
import java.awt.image.*;

import static OverflowGateBot.lib.minigame.tictactoe.TicTacToeConfig.*;

public class TicTacToe extends SimpleTable {

    private UserData player1;
    private UserData player2;

    private final BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    private final Graphics2D canvas = image.createGraphics();

    public TicTacToe(SlashCommandInteractionEvent event) { super(event, 10); }

    public void init() {

    }

    public void start() {

    }

    public void reset() {

    }

    public void addPlayer(User user) {
        if (player1 == null) {
            player1 = 
        }
    }
}
