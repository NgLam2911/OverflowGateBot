package OverflowGateBot;

import java.io.File;
import java.io.IOException;

import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessagesHandler;
import OverflowGateBot.main.UserHandler;
import OverflowGateBot.mindustry.ContentHandler;
import OverflowGateBot.mindustry.ONet;
import OverflowGateBot.mindustry.ServerStatus;
import OverflowGateBot.misc.JSONHandler;
import OverflowGateBot.misc.JSONHandler.JSONData;

import arc.util.Log;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class OverflowGateBot {
    public final static int saveInterval = 1 * 60 * 1000;

    public final static String dailyFilePath = "cache/data/daily";
    public final static String userFilePath = "cache/data/user/user";
    public final static String guildFilePath = "cache/data/guild/guild";
    public final static String serverFilePath = "cache/data/server";
    public final static String guessTheNumberPath = "cache/data/guessTheNumber";
    public static final String sharId = "719322804549320725";

    public static JDA jda = null;

    public static MessagesHandler messagesHandler;
    public static GuildHandler guildHandler;
    public static ContentHandler contentHandler;
    public static CommandHandler commandHandler;
    public static ContextMenuHandler contextMenuHandler;
    public static ONet onet;
    public static UserHandler userHandler;
    public static ServerStatus serverStatus;

    public static void main(String[] args) {
        try {

            File file = new File("token.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            JSONHandler jsonHandler = new JSONHandler();

            JSONData reader = (jsonHandler.new JSONReader("token.json")).read();
            String token = reader.readString("token", null);

            // Build jda
            jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(CacheFlag.VOICE_STATE).build();

            jda.awaitReady();

            onet = new ONet();
            messagesHandler = new MessagesHandler();
            guildHandler = new GuildHandler();
            userHandler = new UserHandler();
            contentHandler = new ContentHandler();
            commandHandler = new CommandHandler();
            contextMenuHandler = new ContextMenuHandler();
            serverStatus = new ServerStatus();

            Log.info("Setup done");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // TODO Database
        onet.run(0, saveInterval, () -> save());
    }

    public static void save() {
        try {
            userHandler.save();
            serverStatus.save();
            guildHandler.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
