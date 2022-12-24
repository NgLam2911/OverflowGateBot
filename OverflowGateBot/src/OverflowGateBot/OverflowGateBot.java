package OverflowGateBot;

import OverflowGateBot.lib.mindustry.ContentHandler;
import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessagesHandler;
import OverflowGateBot.main.NetworkHandler;
import OverflowGateBot.main.ServerStatusHandler;
import OverflowGateBot.main.UserHandler;
import arc.util.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class OverflowGateBot {

    public static JDA jda = null;

    public static final String SHAR_ID = "719322804549320725";

    public static final long UPDATE_PERIOD = 60 * 1000l;
    public static final int GUILD_ALIVE_TIME = 10;
    public static final int USER_ALIVE_TIME = 10;
    public static final int UPDATE_LIMIT = 10;

    public static final String DATABASE_URL = "mongodb+srv://bot1:imthebot1@cluster0.7omeswq.mongodb.net/?retryWrites=true&w=majority";
    public static final String GUILD_COLLECTION = "GUILD_DATA";
    public static final Long MAX_LOG_COUNT = 10000l;
    public static final String TIME_INSERT_STRING = "_timeInserted";

    public static MessagesHandler messagesHandler;
    public static GuildHandler guildHandler;
    public static ContentHandler contentHandler;
    public static CommandHandler commandHandler;
    public static ContextMenuHandler contextMenuHandler;
    public static NetworkHandler networkHandler;
    public static UserHandler userHandler;
    public static ServerStatusHandler serverStatusHandler;
    public static DatabaseHandler databaseHandler;

    public static void main(String[] args) {

        try {
            String token = System.getenv("TOKEN");

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
            serverStatusHandler = new ServerStatusHandler();
            // updateCommand();

            networkHandler.run("UPDATE", 0, UPDATE_PERIOD, () -> update());

            Log.info("Bot online");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void update() {
        userHandler.update();
        guildHandler.update();
        serverStatusHandler.update();
        jda.getPresence()
                .setActivity(Activity
                        .playing("with " + guildHandler.guildCache.size() + " servers | " + userHandler.userCache.size()
                                + " users"));
    }

    public static void updateCommand() {
        jda.updateCommands().complete();
        commandHandler.commands.values().forEach(c -> jda.upsertCommand(c.command).complete());
        contextMenuHandler.commands.values().forEach(c -> jda.upsertCommand(c.command).complete());
    }
}
