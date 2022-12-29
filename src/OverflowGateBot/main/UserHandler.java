package OverflowGateBot.main;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

import OverflowGateBot.lib.user.GuildData;
import OverflowGateBot.lib.user.UserData;
import OverflowGateBot.main.DatabaseHandler.DATABASE;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;
import arc.util.Log;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    // Hash map to store user cache
    public ConcurrentHashMap<String, UserData> userCache = new ConcurrentHashMap<>();

    public UserHandler() {

        Log.info("User handler up");
    }

    public void update() {
        updateCache();
    }

    public void updateCache() {
        Iterator<UserData> iterator = userCache.values().iterator();
        while (iterator.hasNext()) {
            UserData user = iterator.next();
            if (!user.isAlive(1)) {
                Log.info("User <" + user.userId + "> offline");
                user.update();
                iterator.remove();

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
            throw new IllegalStateException(BotException.MEMBER_IS_NULL.name());
        }
        UserData user = getUserAwait(member);
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
        if (isShar(member))
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
        Log.info("User <" + userId + "> online");
        return userData;
    }

    // Add user to cache
    public UserData addUser(Member member) {
        return addUser(member.getGuild().getId(), member.getId());
    }

    // Get user without adding it to cache
    public UserData getUserNoCache(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);
        return new UserData(guildId, userId);
    }

    // Waiting for data from database
    public UserData getUserAwait(@Nonnull Member member) {
        String guildId = member.getGuild().getId();
        String userId = member.getId();
        // If user exist in cache then return, else query user from database
        String hashId = guildId + userId;
        if (userCache.containsKey(hashId))
            return userCache.get(hashId);

        UserData userFromCache = addUser(guildId, userId);
        UserData userFromDatabase = getUserFromDatabase(guildId, userId);
        userFromDatabase._merge(userFromCache);
        userCache.put(hashId, userFromDatabase);
        return userFromDatabase;
    }

    public ConcurrentHashMap<String, UserData> getUserFromGuild(@Nonnull String guildId) {
        ConcurrentHashMap<String, UserData> users = new ConcurrentHashMap<String, UserData>();
        userCache.values().forEach(user -> {
            if (user.guildId.equals(guildId))
                users.put(user.userId, user);
        });
        return users;
    }

    public UserData getUserFromDatabase(@Nonnull String guildId, @Nonnull String userId) {
        // User from a new guild
        if (!DatabaseHandler.collectionExists(DATABASE.USER, guildId)) {
            DatabaseHandler.getDatabase(DATABASE.USER).createCollection(guildId);
            DatabaseHandler.log(LOG_TYPE.DATABASE, new Document().append("NEW GUILD", guildId));
            return new UserData(guildId, userId);

        }
        MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(guildId,
                UserData.class);

        // Get user from database
        Bson filter = new Document().append("userId", userId);
        UserData data = collection.find(filter).limit(1).first();
        if (data == null)
            return new UserData(guildId, userId);
        else
            return data;
    }
}
