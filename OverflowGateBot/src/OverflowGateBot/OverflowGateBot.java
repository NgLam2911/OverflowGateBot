package OverflowGateBot;


import java.io.IOException;

import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.GuildConfigHandler;
import OverflowGateBot.main.MessagesHandler;
import OverflowGateBot.mindustry.ContentHandler;
import OverflowGateBot.mindustry.ONet;
import OverflowGateBot.mindustry.ServerStatus;
import OverflowGateBot.user.UserHandler;

public class OverflowGateBot {
    public final static int saveInterval = 5 * 60 * 1000;

    public final static String dailyFilePath = "cache/data/daily";
    public final static String userFilePath = "cache/data/user";
    public final static String guildFilePath = "cache/data/guild";
    public final static String serverFilePath = "cache/data/server";

    public static MessagesHandler messagesHandler = new MessagesHandler();
    public static GuildConfigHandler guildConfigHandler = new GuildConfigHandler();
    public static ContentHandler contentHandler = new ContentHandler();
    public static CommandHandler commandHandler = new CommandHandler();
    public static ONet onet = new ONet();
    public static UserHandler userHandler = new UserHandler();
    public static ServerStatus serverStatus = new ServerStatus();

    public static void main(String[] args) {
        // TODO Database
        onet.run(0, saveInterval, () -> save());
    }

    public static void save() {
        try {
            userHandler.save();
            serverStatus.save();
            guildConfigHandler.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
