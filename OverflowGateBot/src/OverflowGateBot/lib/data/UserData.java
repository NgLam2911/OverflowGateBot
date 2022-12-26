package OverflowGateBot.lib.data;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.lib.data.GuildData.BOOLEAN_STATE;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;
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
    public String name = new String();
    public String showLevel = BOOLEAN_STATE.UNSET.name();

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

    public UserData modify(@Nonnull String guildId, @Nonnull String userId, String name, Integer point, Integer level,
            Integer money, Integer pvpPoint) {
        this.userId = userId;
        this.guildId = guildId;
        this.name = name;
        this.point = point;
        this.level = level;
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

    public void setName(String name) {
        this.name = name;
        update();
    }

    public String getName() {
        return this.name;
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

    public void setShowLevel(String showLevel) {
        this.showLevel = showLevel;
    }

    public String getShowLevel() {
        return this.showLevel;
    }

    @Override
    public String toString() {
        return "userId:" + this.userId + "\n"
                + "guildId:" + this.guildId + "\n"
                + "point:" + this.point + "\n"
                + "level:" + this.level + "\n"
                + "showLevel:" + this.showLevel + "\n";
    }

    public Document toDocument() {
        return new Document().append("userId", this.userId).//
                append("guildId", this.guildId).//
                append("point", this.point).//
                append("level", this.level).//
                append("showLevel", this.showLevel);
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
        return member;
    }

    public Guild _getGuild() {
        Guild guild = jda.getGuildById(this.guildId);
        if (guild == null)
            throw new IllegalStateException("Guild not found with id <" + guildId + ">");
        return guild;
    }

    public String _getName() {
        return _getMember().getEffectiveName();
    }

    public void _displayLevelName() {
        Member bot = _getGuild().getMember(jda.getSelfUser());
        if (bot == null)
            throw new IllegalStateException("Bot not in guild " + guildId);

        Member member = _getMember();
        if (member == null)
            return;
        // Loop through guild members and modify their nickname
        if (bot.canInteract(member)) {

            String nickname = this.name;
            if (nickname.isBlank())
                nickname = _getMember().getUser().getName();
            if (showLevel.equalsIgnoreCase(BOOLEAN_STATE.TRUE.name()))
                nickname = "[Lv" + this.level + "] " + nickname;
            member.modifyNickname(nickname).complete();
        }
    }

    // Add point for user
    public boolean _addPoint(int p) {
        boolean levelUp = false;
        int extra;

        while (point + p >= _getLevelCap()) {
            extra = _getLevelCap() - point;
            point = 0;
            p -= extra;
            level += 1;
            levelUp = true;
            _displayLevelName();
            _checkLevelRole();
        }
        point += p;

        update(1);
        return levelUp;
    }

    public int _addMoney(int m) {
        this.money += m;
        update(1);
        return m;
    }

    public int _addPVPPoint(int m) {
        this.pvpPoint += m;
        update();
        return m;
    }

    // Add role to member when level is satisfied
    public void _checkLevelRole() {
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

    public UserData _merge(UserData data) {
        if (data == null)
            return this;
        modify(guildId, userId, name, point, level, money + data.money, pvpPoint + data.pvpPoint)
                ._addPoint(data._getTotalPoint());
        if (this.showLevel.equalsIgnoreCase(BOOLEAN_STATE.UNSET.name()))
            this.showLevel = data.showLevel;

        return this;
    }

    // Update user on database
    @Override
    public void update() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DATABASE.USER, this.guildId))
            DatabaseHandler.createCollection(DATABASE.USER, this.guildId);

        MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(this.guildId,
                UserData.class);

        if (this.showLevel.equalsIgnoreCase(BOOLEAN_STATE.UNSET.name())) {
            this.showLevel = BOOLEAN_STATE.FALSE.name();
        }

        // Filter for user id, user id is unique for each collection
        Bson filter = new Document().append("userId", this.userId);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
        DatabaseHandler.log(LOG_TYPE.DATABASE, new Document().append("UPDATE USER", this.toDocument()));
    }
}
