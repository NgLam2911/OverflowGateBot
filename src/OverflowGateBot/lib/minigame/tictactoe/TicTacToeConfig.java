package OverflowGateBot.lib.minigame.tictactoe;

import java.awt.*;

public final class TicTacToeConfig {

    public static final int CANVAS_HEIGHT = 200;
    public static final int CANVAS_WIDTH = 200;
    public static final int CELLS = 3;

    public static final int CELL_SIZE = Math.min(CANVAS_WIDTH, CANVAS_HEIGHT) / CELLS;

    public static final int X = 1;
    public static final int Y = 2;
    public static final int DEFAULT = 0;

    public static final Color DEFAULT_BOARD_COLOR = new Color(255, 255, 255, 255);
    public static final Font FONT = new Font("TimesRoman", Font.PLAIN, (int) (CELL_SIZE * 0.8));

    private static class LazyHolder { static final TicTacToeConfig INSTANCE = new TicTacToeConfig(); }

    public static TicTacToeConfig getInstance() { return LazyHolder.INSTANCE; }
}
