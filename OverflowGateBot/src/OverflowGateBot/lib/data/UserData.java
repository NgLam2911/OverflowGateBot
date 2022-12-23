package OverflowGateBot.lib.data;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.main.DatabaseHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

import java.util.concurrent.ConcurrentHashMap;

public class UserData extends DataCache {

    @Nonnull
    public String userId;
    @Nonnull
    public String guildId;
    public Integer point = 0;
    public Integer level = 0;
    public Integer money = 0;
    public Integer pvpPoint = 0;
    public Boolean hideLevel = false;

    // For codec
    public UserData() {
        super(USER_ALIVE_TIME, UPDATE_LIMIT);
        userId = new String();
        guildId = new String();
    }

    public UserData(@Nonnull String guildId, @Nonnull String userId) {
        super(USER_ALIVE_TIME, UPDATE_LIMIT);
        this.userId = userId;
        this.guildId = guildId;
    }

    public UserData modify(@Nonnull String guildId, @Nonnull String userId, Integer point, Integer level,
            Integer money, Integer pvpPoint, Boolean hideLevel) {
        this.userId = userId;
        this.guildId = guildId;
        this.point = point;
        this.level = level;
        this.hideLevel = hideLevel;
        this.money = money;
        this.pvpPoint = pvpPoint;
        return this;
    }

    public void setId(@Nonnull String userId) {
        this.userId = userId;
    }

    public String getId() {
        return this.userId;
    }

    public void setGuildId(@Nonnull String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return this.guildId;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getPoint() {
        return this.point;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    public void setHideLevel(boolean hideLevel) {
        this.hideLevel = hideLevel;
    }

    public boolean getHideLevel() {
        return this.hideLevel;
    }

    @Override
    public String toString() {
        return "userId:" + this.userId + "\n"
                + "guildId:" + this.guildId + "\n"
                + "point:" + this.point + "\n"
                + "level:" + this.level + "\n"
                + "hideLevel:" + this.hideLevel + "\n";
    }

    public Document toDocument() {
        return new Document().append("userId", this.userId).//
                append("guildId", this.guildId).//
                append("point", this.point).//
                append("level", this.level).//
                append("hideLevel", this.hideLevel);
    }

    public String _getHashId() {
        return this.guildId + this.userId;
    }

    public int _getLevelCap() {
        return level * level;
    }

    public Integer _getTotalPoint() {
        return ((level - 1) * level * (2 * (level - 1) + 1) / 6) + point;
    }

    public Member _getMember() {
        Guild guild = _getGuild();
        Member member = guild.getMemberById(userId);
        if (member == null)
            throw new IllegalStateException(
                    "Member not found with id <" + userId + "> in guild with guild id <" + guildId + ">");
        return member;
    }

    public Guild _getGuild() {
        Guild guild = jda.getGuildById(this.guildId);
        if (guild == null)
            throw new IllegalStateException("Guild not found with id <" + guildId + ">");
        return guild;
    }

    // Add point for user
    public boolean addPoint(int p) {
        boolean levelUp = false;
        int extra;

        while (point + p >= _getLevelCap()) {
            extra = _getLevelCap() - point;
            point = 0;
            p -= extra;
            level += 1;
            levelUp = true;
        }
        point += p;

        update(1);
        checkLevelRole();
        return levelUp;
    }

    // Add role to member when level is satisfied
    public void checkLevelRole() {
        ConcurrentHashMap<String, Integer> levelRoleId = guildHandler.getGuild(this.guildId).levelRoleId;
        Guild guild = _getGuild();
        Member bot = guild.getMember(jda.getSelfUser());
        Member member = _getMember();
        if (bot == null || member == null)
            return;
        // If bot has a higher role position
        if (!bot.canInteract(member))
            return;
        for (String key : levelRoleId.keySet()) {
            if (levelRoleId.get(key) <= this.level) {
                if (key == null)
                    return;
                Role role = guild.getRoleById(key);
                if (role == null)
                    return;
                guild.addRoleToMember(member, role);
            }
        }
    }

    public UserData mergeUser(UserData data) {
        if (data == null)
            return this;
        modify(guildId, userId, point, level, money + data.money, pvpPoint + data.pvpPoint, hideLevel)
                .addPoint(data._getTotalPoint());
        return this;
    }

    // Update user on database
    @Override
    public void update() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, this.guildId))
            DatabaseHandler.userDatabase.createCollection(this.guildId);

        MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(this.guildId,
                UserData.class);

        // Filter for user id, user id is unique for each collection
        Bson filter = new Document().append("userId", this.userId);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
    }
}
