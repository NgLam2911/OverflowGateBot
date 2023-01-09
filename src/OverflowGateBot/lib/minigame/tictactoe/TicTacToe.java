package OverflowGateBot.lib.minigame.tictactoe;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import OverflowGateBot.lib.discord.table.SimpleEmbed;
import OverflowGateBot.lib.user.UserData;
import OverflowGateBot.main.UserHandler;

import static OverflowGateBot.lib.minigame.tictactoe.TicTacToeConfig.*;

public class TicTacToe extends SimpleEmbed {

    private UserData player1;
    private UserData player2;
    private Integer currentPlayer = X;
    private Integer move = 0;

    private int[][] board = new int[3][3];
    private EmbedBuilder builder = new EmbedBuilder();

    public TicTacToe(SlashCommandInteractionEvent event) { super(event, 10); }

    public void init() {
        player1 = null;
        player2 = null;
        resetBoard();
        clearButton();
        builder.clear();
        builder.setTitle("*Cờ ca rô*");
        addButtonPrimary("Chơi", () -> this.addPlayer());
        addButtonDeny("X", () -> this.delete());
        updateBoard();
    }

    private void resetBoard() {
        resetTimer();
        clearButton();
        currentPlayer = X;
        move = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = DEFAULT;
            }
        }
    }

    public void addPlayer() {
        resetTimer();
        Member member = getTriggerMember();
        UserData user = UserHandler.getUserAwait(member);
        if (player1 == null) {
            player1 = user;
            clearButton();
            builder.clear();
            builder.setTitle("*Cờ ca rô*");
            builder.addField("Player 1", player1.getName(), false);
            addButtonPrimary("Tham gia", () -> this.addPlayer());
            updateBoard();

        } else if (user != player1) {
            player2 = user;
            builder.clear();
            builder.setTitle("*Cờ ca rô*");
            builder.addField("Lượt đi của", "PLayer 1: " + player1.getName(), false);
            resetBoard();
            updateButton();
            updateBoard();
        } else
            sendMessage("Bạn đa tham gia, hãy mời người chơi khác tham gia", true);
    }

    private void onMove() {
        try {
            resetTimer();
            Member member = getTriggerMember();
            builder.setTitle("*Cờ ca rô*");

            if (currentPlayer == X) {
                if (player1 != UserHandler.getUserAwait(member)) {
                    sendMessage("Đây là lượt của người khác, bạn không có quyền tường tác", true);
                    return;
                }
            } else {
                if (player2 != UserHandler.getUserAwait(member)) {
                    sendMessage("Đây là lượt của người khác, bạn không có quyền tường tác", true);
                    return;
                }
            }

            String buttonName = getButtonName();
            String[] buttonId = buttonName.split("-");

            int x = Integer.parseInt(buttonId[1]);
            int y = Integer.parseInt(buttonId[2]);

            if (!isValidMove(x, y)) {
                sendMessage("Nước đi không hợp lệ", true);
                return;
            }

            board[x][y] = currentPlayer;
            move += 1;

            if (!isFinished(x, y)) {

                if (move >= 9) {
                    clearButton();
                    builder.clear();
                    builder.setTitle("*Cờ ca rô*");
                    builder.addField("Kết quả", "Hòa", false);
                    addButtonPrimary("Ván mới", () -> init());
                    updateBoard();
                    return;

                }
                if (currentPlayer == X) {
                    builder.clear();
                    builder.setTitle("*Cờ ca rô*");
                    builder.addField("Lượt đi của", "Player 2: " + player2.getName(), false);
                    currentPlayer = Y;

                } else {
                    builder.clear();
                    builder.setTitle("*Cờ ca rô*");
                    builder.addField("Lượt đi của", "Player 1: " + player1.getName(), false);
                    currentPlayer = X;
                }

                updateButton();
                updateBoard();

            } else {

                clearButton();
                builder.clear();
                builder.setTitle("*Cờ ca rô*");
                builder.addField("Kết quả", "Người thắng: " + (currentPlayer == X ? player1.getName() : player2.getName()), false);
                addButtonPrimary("Ván mới", () -> init());
                updateBoard();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidMove(int x, int y) { return board[x][y] == DEFAULT; }

    private boolean isFinished(int x, int y) {
        for (int i = 0; i < 3; i++) {
            if (board[x][i] != currentPlayer)
                break;
            if (i == 2)
                return true;
        }

        for (int i = 0; i < 3; i++) {
            if (board[i][y] != currentPlayer)
                break;
            if (i == 2)
                return true;
        }

        if (x == y) {
            for (int i = 0; i < 3; i++) {
                if (board[i][i] != currentPlayer)
                    break;
                if (i == 2)
                    return true;
            }
        }

        if (x + y == 2) {
            for (int i = 0; i < 3; i++) {
                if (board[i][(2) - i] != currentPlayer)
                    break;
                if (i == 2)
                    return true;
            }
        }

        return false;
    }

    private void updateButton() {
        clearButton();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == DEFAULT) {
                    addButtonPrimary("-" + i + "-" + j, "-", () -> onMove());
                } else if (board[i][j] == Y) {
                    addButtonSuccess("-" + i + "-" + j, "O", () -> onMove());
                } else
                    addButtonDeny("-" + i + "-" + j, "X", () -> onMove());
            }
            addRow();
        }
    }

    public void updateBoard() { event.getHook().editOriginalEmbeds(builder.build()).setActionRows(getButton()).queue(); }

}
