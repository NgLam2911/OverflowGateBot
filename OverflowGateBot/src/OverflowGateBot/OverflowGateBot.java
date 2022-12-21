package OverflowGateBot;

import java.io.File;
import java.io.IOException;

import OverflowGateBot.lib.data.misc.JSONHandler;
import OverflowGateBot.lib.data.misc.JSONHandler.JSONData;
import OverflowGateBot.lib.mindustry.ContentHandler;
import OverflowGateBot.lib.mindustry.ServerStatus;
import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessagesHandler;
import OverflowGateBot.main.NetworkHandler;
import OverflowGateBot.main.UserHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class OverflowGateBot {

    public static final String SHAR_ID = "719322804549320725";

    public static JDA jda = null;

    public static MessagesHandler messagesHandler;
    public static GuildHandler guildHandler;
    public static ContentHandler contentHandler;
    public static CommandHandler commandHandler;
    public static ContextMenuHandler contextMenuHandler;
    public static NetworkHandler networkHandler;
    public static UserHandler userHandler;
    public static ServerStatus serverStatus;
    public static DatabaseHandler databaseHandler;

    public static void main(String[] args) {
        try {
            System.out.println("Starting");
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

            databaseHandler = new DatabaseHandler();
            networkHandler = new NetworkHandler();
            messagesHandler = new MessagesHandler();
            guildHandler = new GuildHandler();
            userHandler = new UserHandler();
            contentHandler = new ContentHandler();
            commandHandler = new CommandHandler();
            contextMenuHandler = new ContextMenuHandler();
            serverStatus = new ServerStatus();

            System.out.println("Setup done");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
