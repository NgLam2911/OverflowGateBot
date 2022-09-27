package OverflowGateBot;

import java.io.IOException;

public class OverflowGateBot {
    public final static int saveInterval = 6 * 60 * 1000;

    public static ContentHandler contentHandler = new ContentHandler();
    public static CommandHandler commandHandler = new CommandHandler();
    public static ONet onet = new ONet();
    public static MessagesHandler messagesHandler = new MessagesHandler();
    public static UserHandler userHandler = new UserHandler();
    public static ServerStatus serverStatus = new ServerStatus();

    public static void main(String[] args) {
        onet.run(0, saveInterval, () -> {
            try {
                userHandler.save();
                serverStatus.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
