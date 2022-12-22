package OverflowGateBot.main;

import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.lib.data.DataCache;
import OverflowGateBot.lib.data.GuildData;
import net.dv8tion.jda.api.entities.Guild;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildHandler {

    public HashMap<String, GuildCache> guildCache = new HashMap<>();

    public GuildHandler() {

        System.out.println("Guild handler up");
    }

    public void update() {
        updateGuildCache();
    }

    public void updateGuildCache() {
        Iterator<GuildCache> iterator = guildCache.values().iterator();
        while (iterator.hasNext()) {
            GuildCache guild = iterator.next();
            if (!guild.isAlive(1)) {
                iterator.remove();
                updateGuild(guild.data);
            }
        }
    }

    public GuildCache getGuild(Guild guild) {
        if (guild == null)
            return null;
        return getGuild(guild.getId());
    }

    // Add guild to cache
    public GuildCache addGuild(@Nonnull String guildId) {
        GuildData guildData = new GuildData(guildId);
        GuildCache guildCacheData = new GuildCache(guildData);
        guildCache.put(guildId, guildCacheData);
        return guildCacheData;
    }

    // Get guild from cache/database
    public GuildCache getGuild(@Nonnull String guildId) {
        // If guild exist in cache then return, else query guild from database
        if (guildCache.containsKey(guildId))
            return guildCache.get(guildId);

        // Create new guild cache to store temporary guild data

        if (!DatabaseHandler.collectionExists(DatabaseHandler.guildDatabase, GUILD_COLLECTION)) {
            DatabaseHandler.guildDatabase.createCollection(GUILD_COLLECTION);
            return new GuildCache(new GuildData(guildId));

        }
        MongoCollection<GuildData> collection = DatabaseHandler.guildDatabase.getCollection(GUILD_COLLECTION,
                GuildData.class);

        // Get guild from database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        if (data.iterator().hasNext()) {
            GuildCache guildCacheData = guildCache.put(guildId, new GuildCache(data.first()));
            return guildCacheData;
        } else {
            return addGuild(guildId);
        }

    }

    // Update guild on database
    public void updateGuild(GuildData guildData) {
        try {
            // Create collection if it's not exist
            if (!DatabaseHandler.collectionExists(DatabaseHandler.guildDatabase, GUILD_COLLECTION))
                DatabaseHandler.guildDatabase.createCollection(GUILD_COLLECTION);

            MongoCollection<GuildData> collection = DatabaseHandler.guildDatabase.getCollection(GUILD_COLLECTION,
                    GuildData.class);

            // Filter for guild id, guild id is unique for each collection
            Bson filter = new Document().append("guildId", guildData.guildId);
            collection.replaceOne(filter, guildData, new ReplaceOptions().upsert(true));

        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public class GuildCache extends DataCache {
        public GuildData data;

        public GuildCache(GuildData data) {
            super(GUILD_ALIVE_TIME);
            this.data = data;
        }

    }
}
