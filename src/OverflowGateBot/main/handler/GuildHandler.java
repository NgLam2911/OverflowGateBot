package OverflowGateBot.main.handler;

import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import OverflowGateBot.BotConfig;
import OverflowGateBot.main.handler.DatabaseHandler.DATABASE;
import OverflowGateBot.main.user.GuildData;
import arc.util.Log;
import net.dv8tion.jda.api.entities.Guild;

public class GuildHandler {

    private static GuildHandler instance = new GuildHandler();
    private static HashMap<String, GuildData> guildCache = new HashMap<>();

    private GuildHandler() {

        Log.info("Guild handler up");
    }

    @Override
    protected void finalize() {
        Log.info("Guild handler down");
    }

    public static GuildHandler getInstance() {
        return instance;
    }

    public static void update() {
        updateGuildCache();
    }

    public static void updateGuildCache() {
        Iterator<GuildData> iterator = guildCache.values().iterator();
        while (iterator.hasNext()) {
            GuildData guild = iterator.next();
            if (!guild.isAlive(1)) {
                Log.info("Guild <" + guild.guildId + "> offline");
                UpdatableHandler.updateStatus();
                guild.update();
                iterator.remove();

            }
        }
    }

    public static int getActiveGuildCount() {
        return guildCache.size();
    }

    public static GuildData getGuild(Guild guild) {
        if (guild == null)
            return null;
        return getGuild(guild.getId());
    }

    // Add guild to cache
    public static GuildData addGuild(@Nonnull String guildId) {
        GuildData guildData = new GuildData(guildId);
        guildCache.put(guildId, guildData);
        return guildData;
    }

    // Get guild from cache/database
    public static GuildData getGuild(@Nonnull String guildId) {
        // If guild exist in cache then return, else query guild from database
        if (guildCache.containsKey(guildId)) {
            GuildData guildData = guildCache.get(guildId);
            guildData.resetTimer();
            return guildData;
        }

        // Create new guild cache to store temporary guild data
        if (!DatabaseHandler.collectionExists(DATABASE.GUILD, BotConfig.GUILD_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.GUILD, BotConfig.GUILD_COLLECTION);
            return addGuild(guildId);
        }

        MongoCollection<GuildData> collection = DatabaseHandler.getDatabase(DATABASE.GUILD)
                .getCollection(BotConfig.GUILD_COLLECTION, GuildData.class);

        // Get guild from database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        GuildData first = data.first();
        UpdatableHandler.updateStatus();
        if (first != null) {
            Log.info("Guild <" + guildId + "> online");
            guildCache.put(guildId, first);
            return first;
        } else {
            Log.info("New guild <" + guildId + "> online");
            return addGuild(guildId);
        }
    }
}
