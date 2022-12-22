package OverflowGateBot.main;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.lib.data.DataCache;
import OverflowGateBot.lib.data.GuildData;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    // Hash map to store user cache
    public HashMap<String, UserCache> userCache = new HashMap<>();

    public UserHandler() {

        System.out.println("User handler up");
    }

    public void update() {
        updateCache();
    }

    public void updateCache() {
        Iterator<UserCache> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserCache user = iterator.next();
            if (!user.isAlive(1)) {
                iterator.remove();
                updateUser(user.data);
            }
        }
    }

    // Get date for daily command
    public String getDate() {
        return (Calendar.getInstance().getTime()).toString();
    }

    // Update point, money, level on massage sent
    public void onMessage(Message message) {
        Member member = message.getMember();
        if (member == null) {
            System.out.println("Invalid message sender");
            return;
        }
        UserCache userCacheData = getUserInstance(member);
        userCacheData.reset();
        UserData user = userCacheData.data;

        if (user == null) {
            System.out.println("User not exist");
            return;
        }
        user.addPoint(1);
        user.checkLevelRole();
    }

    public boolean isShar(Member member) {
        if (member == null)
            return false;
        return (member.getId().equals(SHAR_ID));
    }

    public boolean isAdmin(Member member) {
        if (member == null)
            return false;

        List<Role> roles = member.getRoles();

        GuildData guildData = guildHandler.getGuild(member.getGuild().getId()).data;
        for (String adminId : guildData.adminRoleId) {
            for (Role role : roles) {
                if (role.getId().equals(adminId))
                    return true;
            }
        }
        return false;
    }

    // Add user to cache
    public UserCache addUser(@Nonnull String guildId, @Nonnull String userId) {
        UserData userData = new UserData(guildId, userId);
        UserCache userCacheData = new UserCache(userData);
        // Key is hashId = guildId + userId
        userCache.put(userCacheData.data._getHashId(), userCacheData);
        return userCacheData;
    }

    // Add user to cache
    public UserCache addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    // Get user from cache and merge with data from database later
    public UserCache getUserInstance(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        UserCache userFromCache = getUserFromCache(member.getGuild().getId(), member.getId());
        networkHandler.run(0, () -> {
            UserCache userFromDatabase = getUserFromDatabase(member.getGuild().getId(), member.getId());
            userFromCache.data.mergeUser(userFromDatabase.data);
            userCache.put(userFromCache.data._getHashId(), userFromCache);
        });

        return userFromCache;
    }

    // Waiting for data from database
    public UserCache getUserAwait(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        UserCache userFromCache = getUserFromCache(guildId, userId);
        UserCache userFromDatabase = getUserFromDatabase(guildId, userId);
        userFromDatabase.data.mergeUser(userFromCache.data);
        userCache.put(guildId + userId, userFromDatabase);
        return userFromDatabase;
    }

    // Get user from cache/database
    public UserCache getUserFromCache(@Nonnull String guildId, @Nonnull String userId) {
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        // Create new user cache to store temporary user data
        return addUser(guildId, userId);
    }

    public UserCache getUserFromDatabase(@Nonnull String guildId, @Nonnull String userId) {
        // User from a new guild
        if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, guildId)) {
            DatabaseHandler.userDatabase.createCollection(guildId);
            DatabaseHandler.log(LOG_TYPE.DATABASE, "Create new guild collection with id " + guildId);
            return new UserCache(new UserData(guildId, userId));

        }
        MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(guildId,
                UserData.class);

        // Get user from database
        Bson filter = new Document().append("userId", userId);
        FindIterable<UserData> data = collection.find(filter).limit(1);

        return new UserCache(data.first());
    }

    // Update user on database
    public void updateUser(UserData user) {
        try {
            // Create collection if it's not exist
            if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, user.guildId))
                DatabaseHandler.userDatabase.createCollection(user.guildId);

            MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(user.guildId,
                    UserData.class);

            // Filter for user id, user id is unique for each collection
            Bson filter = new Document().append("userId", user.userId);
            collection.replaceOne(filter, user, new ReplaceOptions().upsert(true));

        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public class UserCache extends DataCache {

        public UserData data;
        public Member member;

        public UserCache(UserData data) {
            super(USER_ALIVE_TIME);
            this.data = data;
        }

        // Get discord member instance
        public Member getMember() {
            if (member != null)
                return member;

            String guildId = data.guildId;
            String userId = data.userId;

            if (guildId == null || userId == null)
                return null;

            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                return null;

            member = guild.getMemberById(userId);
            return member;
        }
    }
}
