package OverflowGateBot;

import java.io.IOException;

import OverflowGateBot.MiniGame.GuessTheNumberHandler;
import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessagesHandler;
import OverflowGateBot.main.UserHandler;
import OverflowGateBot.mindustry.ContentHandler;
import OverflowGateBot.mindustry.ONet;
import OverflowGateBot.mindustry.ServerStatus;

public class OverflowGateBot {
    public final static int saveInterval = 1 * 60 * 1000;

    public final static String dailyFilePath = "cache/data/daily";
    public final static String userFilePath = "cache/data/user/user";
    public final static String guildFilePath = "cache/data/guild/guild";
    public final static String serverFilePath = "cache/data/server";
    public final static String guessTheNumberPath = "cache/data/guessTheNumber";

    public static MessagesHandler messagesHandler = new MessagesHandler();
    public static GuildHandler guildHandler = new GuildHandler();
    public static ContentHandler contentHandler = new ContentHandler();
    public static CommandHandler commandHandler = new CommandHandler();
    public static ONet onet = new ONet();
    public static UserHandler userHandler = new UserHandler();
    public static ServerStatus serverStatus = new ServerStatus();
    public static GuessTheNumberHandler guessTheNumberHandler = new GuessTheNumberHandler();

    public static void main(String[] args) {
        // TODO Database
        onet.run(0, saveInterval, () -> save());
    }

    public static void save() {
        try {
            userHandler.save();
            serverStatus.save();
            guildHandler.save();
            guessTheNumberHandler.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

