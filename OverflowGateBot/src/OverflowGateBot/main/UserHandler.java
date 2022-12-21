package OverflowGateBot.main;

import java.util.Calendar;
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
import OverflowGateBot.lib.data.user.AlphaUser;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {
    public class UserCache extends DataCache {

        AlphaUser user;
        Member member;

        public UserCache(AlphaUser user) {
            super();
            this.user = user;
        }

        // Get discord member instance
        public Member getMember() {
            if (member != null)
                return member;

            Guild guild = jda.getGuildById(user.guildId);
            if (guild == null)
                return null;

            member = guild.getMemberById(user.userId);
            return member;
        }
    }

    // Hash map to store user cache
    private HashMap<String, UserCache> userCache = new HashMap<>();

    private final int USER_CACHE_UPDATE_PERIOD = 60000; // 1 minutes

    public UserHandler() {

        networkHandler.run(0, USER_CACHE_UPDATE_PERIOD, () -> this.updateCache());

        System.out.println("User handler up");
    }

    public void updateCache() {
        Iterator<UserCache> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserCache user = iterator.next();
            if (!user.isAlive(1)) {
                iterator.remove();
                System.out.println("Sending " + user.getMember().getEffectiveName() + " to database");
                updateUser(user.user);
            }
        }
        jda.getPresence()
                .setActivity(Activity
                        .playing("with " + jda.getGuilds().size() + " servers | " + userCache.size() + " users"));
    }

    // Get date for daily command
    public String getDate() {
        return (Calendar.getInstance().getTime()).toString();
    }

    // Update point, money, level on massage sent
    public void onMessage(Message message) {
        Member member = message.getMember();
        if (member == null)
            return;
        AlphaUser user = getUser(member).user;

        if (user == null)
            return;
        user.addPoint(1);
        user.addMoney(1);
        user.checkMemberRole();
    }

    // Add user to cache
    public UserCache addUser(@Nonnull String guildId, @Nonnull String userId) {
        AlphaUser alphaUser = new AlphaUser(guildId, userId);
        UserCache userCacheData = new UserCache(alphaUser);
        // Key is hashId = guildId + userId
        userCache.put(userCacheData.user._getHashId(), userCacheData);
        return userCacheData;
    }

    // Add user to cache
    public UserCache addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    // Get user from cache/database
    public UserCache getUser(Member member) {
        return getUser(member.getGuild().getId(), member.getId());
    }

    // Get user from cache/database
    public UserCache getUser(@Nonnull String guildId, @Nonnull String userId) {
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        // Create new user cache to store temporary user
        UserCache user = addUser(guildId, userId);
        userCache.put(user.user._getHashId(), user);

        try {
            // User from a new guild
            if (!DatabaseHandler.collectionExists(guildId)) {
                DatabaseHandler.database.createCollection(guildId);
                return user;
            }
            System.out.println("Requesting " + user.getMember().getEffectiveName() + " from database");
            MongoCollection<AlphaUser> collection = DatabaseHandler.database.getCollection(guildId,
                    AlphaUser.class);

            // Get user from database
            Bson filter = new Document().append("userId", userId);
            FindIterable<AlphaUser> data = collection.find(filter).limit(1);
            // Merge data from cache with data from database
            AlphaUser temp = data.first().mergeUser(user.user);
            return userCache.put(temp._getHashId(), new UserCache(temp));

        } catch (MongoException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Update user on database
    public void updateUser(AlphaUser user) {
        try {
            // Create collection if it's not exist
            if (!DatabaseHandler.collectionExists(user.guildId))
                DatabaseHandler.database.createCollection(user.guildId);

            MongoCollection<AlphaUser> collection = DatabaseHandler.database.getCollection(user.guildId,
                    AlphaUser.class);

            // Filter for user id, user id is unique for each collection
            Bson filter = new Document().append("userId", user.userId);
            collection.replaceOne(filter, user, new ReplaceOptions().upsert(true));

        } catch (MongoException e) {
            e.printStackTrace();
        }
    }
}
