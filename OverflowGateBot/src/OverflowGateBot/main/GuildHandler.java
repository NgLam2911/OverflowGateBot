package OverflowGateBot.main;

import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import OverflowGateBot.lib.data.GuildData;

import net.dv8tion.jda.api.entities.Guild;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildHandler {

    public HashMap<String, GuildData> guildCache = new HashMap<>();

    public GuildHandler() {

        System.out.println("Guild handler up");
    }

    public void update() {
        updateGuildCache();
    }

    public void updateGuildCache() {
        Iterator<GuildData> iterator = guildCache.values().iterator();
        while (iterator.hasNext()) {
            GuildData guild = iterator.next();
            if (!guild.isAlive(1)) {
                iterator.remove();
                guild.update();
            }
        }
    }

    public GuildData getGuild(Guild guild) {
        if (guild == null)
            return null;
        return getGuild(guild.getId());
    }

    // Add guild to cache
    public GuildData addGuild(@Nonnull String guildId) {
        GuildData guildData = new GuildData(guildId);
        guildCache.put(guildId, guildData);
        return guildData;
    }

    // Get guild from cache/database
    public GuildData getGuild(@Nonnull String guildId) {
        // If guild exist in cache then return, else query guild from database
        if (guildCache.containsKey(guildId))
            return guildCache.get(guildId);

        // Create new guild cache to store temporary guild data

        if (!DatabaseHandler.collectionExists(DatabaseHandler.guildDatabase, GUILD_COLLECTION)) {
            DatabaseHandler.guildDatabase.createCollection(GUILD_COLLECTION);
            return new GuildData(guildId);

        }
        MongoCollection<GuildData> collection = DatabaseHandler.guildDatabase.getCollection(GUILD_COLLECTION,
                GuildData.class);

        // Get guild from database
        Bson filter = new Document().append("guildId", guildId);
        FindIterable<GuildData> data = collection.find(filter).limit(1);
        GuildData first = data.first();
        if (first != null) {
            GuildData guildCacheData = guildCache.put(guildId, first);
            return guildCacheData;
        } else {
            return addGuild(guildId);
        }
    }
}
