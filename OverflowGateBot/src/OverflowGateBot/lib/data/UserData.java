package OverflowGateBot.lib.data;

import javax.annotation.Nonnull;

import org.bson.Document;

public class UserData {

    public String userId;
    public String guildId;
    public Integer point = 0;
    public Integer level = 0;
    public Integer money = 0;
    public Integer pvpPoint = 0;
    public Boolean hideLevel = false;

    // For codec
    public UserData() {
    }

    public UserData(@Nonnull String guildId, @Nonnull String userId) {
        this.userId = userId;
        this.guildId = guildId;
    }

    public UserData modify(String guildId, String userId, Integer point, Integer level,
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

    // Get level cap
    public int _getLevelCap() {
        return level * level;
    }

    // Math!!!
    public Integer _getTotalPoint() {
        return ((level - 1) * level * (2 * (level - 1) + 1) / 6) + point;
    }

    // Add point for user
    public boolean addPoint(int p) {
        boolean lvUp = false;
        int extra;

        while (point + p >= _getLevelCap()) {
            extra = _getLevelCap() - point;
            point = 0;
            p -= extra;
            level += 1;
            lvUp = true;
            checkLevelRole();
        }
        point += p;
        return lvUp;
    }

    public void checkLevelRole() {
    }

    public UserData mergeUser(UserData data) {
        if (data == null)
            return this;
        modify(guildId, userId, point, level, money + data.money, pvpPoint + data.pvpPoint, hideLevel)
                .addPoint(data._getTotalPoint());
        return this;
    }
}
