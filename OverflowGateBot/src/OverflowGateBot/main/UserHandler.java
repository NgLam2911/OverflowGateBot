package OverflowGateBot.main;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import OverflowGateBot.lib.data.GuildData;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    // Hash map to store user cache
    public HashMap<String, UserData> userCache = new HashMap<>();

    public UserHandler() {

        System.out.println("User handler up");
    }

    public void update() {
        updateCache();
    }

    public void updateCache() {
        Iterator<UserData> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserData user = iterator.next();
            if (!user.isAlive(1)) {
                iterator.remove();
                user.update();
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
        UserData user = getUserInstance(member);
        user.reset();

        user._addMoney(1);
        user._addPoint(1);
        user._checkLevelRole();
    }

    public boolean isShar(Member member) {
        if (member == null)
            return false;
        return (member.getId().equals(SHAR_ID));
    }

    public boolean isAdmin(Member member) {
        if (member == null)
            return false;
        if (member.isOwner())
            return true;
        List<Role> roles = member.getRoles();

        GuildData guildData = guildHandler.getGuild(member.getGuild().getId());
        for (String adminId : guildData.adminRoleId) {
            for (Role role : roles) {
                if (role.getId().equals(adminId))
                    return true;
            }
        }
        return false;
    }

    // Add user to cache
    public UserData addUser(@Nonnull String guildId, @Nonnull String userId) {
        UserData userData = new UserData(guildId, userId);
        // Key is hashId = guildId + userId
        userCache.put(userData._getHashId(), userData);
        return userData;
    }

    // Add user to cache
    public UserData addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    // Get user from cache and merge with data from database later
    public UserData getUserInstance(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        UserData userFromCache = getUserFromCache(member.getGuild().getId(), member.getId());
        networkHandler.run(0, () -> {
            UserData userFromDatabase = getUserFromDatabase(member.getGuild().getId(), member.getId());
            userFromCache._mergeUser(userFromDatabase);
            userCache.put(userFromCache._getHashId(), userFromCache);
        });

        return userFromCache;
    }

    // Waiting for data from database
    public UserData getUserAwait(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        UserData userFromCache = getUserFromCache(guildId, userId);
        UserData userFromDatabase = getUserFromDatabase(guildId, userId);
        userFromDatabase._mergeUser(userFromCache);
        userCache.put(guildId + userId, userFromDatabase);
        return userFromDatabase;
    }

    // Get user from cache/database
    public UserData getUserFromCache(@Nonnull String guildId, @Nonnull String userId) {
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        // Create new user cache to store temporary user data
        return addUser(guildId, userId);
    }

    public UserData getUserFromDatabase(@Nonnull String guildId, @Nonnull String userId) {
        // User from a new guild
        if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, guildId)) {
            DatabaseHandler.userDatabase.createCollection(guildId);
            DatabaseHandler.log(LOG_TYPE.DATABASE, "Create new user collection with guild id " + guildId);
            return new UserData(guildId, userId);

        }
        MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(guildId,
                UserData.class);

        // Get user from database
        Bson filter = new Document().append("userId", userId);
        FindIterable<UserData> data = collection.find(filter).limit(1);

        return data.first();
    }
}
