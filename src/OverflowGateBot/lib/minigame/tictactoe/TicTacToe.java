package OverflowGateBot.lib.minigame.tictactoe;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import OverflowGateBot.lib.discord.table.SimpleEmbed;
import OverflowGateBot.lib.user.UserData;
import OverflowGateBot.main.UserHandler;

import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import static OverflowGateBot.lib.minigame.tictactoe.TicTacToeConfig.*;

public class TicTacToe extends SimpleEmbed {

    private UserData player1;
    private UserData player2;
    private Integer currentPlayer = X;

    private int[][] board = new int[3][3];
    private EmbedBuilder builder = new EmbedBuilder();

    private BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    private Graphics2D canvas = image.createGraphics();

    public TicTacToe(SlashCommandInteractionEvent event) { super(event, 10); }

    public void init() {
        clearButton();
        builder.clear();
        builder.setTitle("*Cờ ca rô*");
        addButtonPrimary("Chơi", () -> this.addPlayer());
        updateBoard();
    }

    private void resetBoard() {
        resetTimer();
        clearButton();
        currentPlayer = X;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = DEFAULT;
            }
        }
        canvas.setBackground(Color.BLACK);
        for (int i = 0; i < CELLS + 1; i++) {
            drawLine(canvas, CELL_SIZE * i, 0, CELL_SIZE * i, CANVAS_HEIGHT, Color.WHITE);
            drawLine(canvas, 0, CELL_SIZE * i, CANVAS_WIDTH, CELL_SIZE * i, Color.WHITE);
        }
        updateButton();
    }

    public void drawLine(Graphics2D graphics, int x1, int y1, int x2, int y2, Color color) {
        graphics.setColor(color);
        graphics.drawLine(x1, y1, x2, y2);
    }

    public void drawString(Graphics2D graphics, int x, int y, String str) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(FONT);
        graphics.drawString(str, x, y);
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
            builder.addField("Lượt đi của", player1.getName(), false);
            resetBoard();
            updateBoard();
        }
    }

    private void onMove() {
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
        String[] buttonId = buttonName.split("|");

        int x = Integer.parseInt(buttonId[0]);
        int y = Integer.parseInt(buttonId[1]);

        if (!isValidMove(x, y)) {
            sendMessage("Nước đi không hợp lệ", true);
            return;
        }

        canvas = image.createGraphics();

        if (currentPlayer == X)
            drawString(canvas, x, y, "X");
        else
            drawString(canvas, x, y, "Y");

        board[x][y] = currentPlayer;

        if (!isFinished(x, y)) {

            if (currentPlayer == X) {
                builder.clear();
                builder.setTitle("*Cờ ca rô*");
                builder.addField("Lượt đi của", player1.getName(), false);
                currentPlayer = Y;
            } else {
                builder.clear();
                builder.setTitle("*Cờ ca rô*");
                builder.addField("Lượt đi của", player1.getName(), false);
                currentPlayer = X;
            }

            updateButton();
            updateBoard();
        } else {
            finish();
            updateBoard();
        }
    }

    private void finish() {
        clearButton();
        builder.clear();
        builder.setTitle("*Cờ ca rô*");
        builder.addField("Người thắng: ", currentPlayer == X ? player1.getName() : player2.getName(), false);
        addButtonPrimary("Ván mới", () -> init());

    }

    private boolean isValidMove(int x, int y) { return board[x][y] == DEFAULT; }

    private boolean isFinished(int x, int y) {
        for (int i = 0; i < 3; i++) {
            if (board[x][i] != currentPlayer)
                break;
            if (i == 3 - 1)
                return true;
        }

        for (int i = 0; i < 3; i++) {
            if (board[i][y] != currentPlayer)
                break;
            if (i == 3 - 1)
                return true;
        }

        if (x == y) {
            for (int i = 0; i < 3; i++) {
                if (board[i][i] != currentPlayer)
                    break;
                if (i == 3 - 1)
                    return true;
            }
        }

        if (x + y == 3 - 1) {
            for (int i = 0; i < 3; i++) {
                if (board[i][(3 - 1) - i] != currentPlayer)
                    break;
                if (i == 3 - 1)
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
                    addButtonSuccess(i + "|" + j, "O", () -> onMove());
                } else if (board[i][j] == currentPlayer) {
                    addButtonSuccess(i + "|" + j, getCurrentPlayer(), () -> onMove());
                } else
                    addButtonDeny(i + "|" + j, getAnotherPlayer(), () -> onMove());
            }
            addRow();
        }
    }

    public void updateBoard() {
        try {

            canvas.dispose();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            builder.setImage("attachment://img");

            event.getHook().editOriginalEmbeds(builder.build()).setActionRows(getButton()).addFile(is, "img").queue();
        } catch (IOException e) {
            reply("Lỗi", 10);
        }

    }

    private @Nonnull String getCurrentPlayer() {
        if (currentPlayer == X)
            return "X";
        return "Y";
    }

    private @Nonnull String getAnotherPlayer() {
        if (currentPlayer == X)
            return "Y";
        return "X";
    }
}
