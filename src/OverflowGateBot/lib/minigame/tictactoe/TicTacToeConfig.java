package OverflowGateBot.lib.minigame.tictactoe;

public final class TicTacToeConfig {

    public static final int X = 1;
    public static final int Y = 2;
    public static final int DEFAULT = 0;

    private static class LazyHolder { static final TicTacToeConfig INSTANCE = new TicTacToeConfig(); }

    public static TicTacToeConfig getInstance() { return LazyHolder.INSTANCE; }
}
