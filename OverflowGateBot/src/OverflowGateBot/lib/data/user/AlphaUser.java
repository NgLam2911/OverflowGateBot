package OverflowGateBot.lib.data.user;

import java.util.HashMap;

import javax.annotation.Nonnull;

import org.bson.Document;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

public class AlphaUser {
    @Nonnull
    public String userId;
    @Nonnull
    public String guildId;
    public Integer point = 0;
    public Integer level = 0;
    public Integer money = 0;
    public Integer pvpPoint = 0;
    public Boolean hideLevel = false;

    public AlphaUser(@Nonnull String guildId, @Nonnull String userId) {
        this.userId = userId;
        this.guildId = guildId;
    }

    public AlphaUser modify(@Nonnull String guildId, @Nonnull String userId, Integer point, Integer level,
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
            checkMemberRole();
        }
        point += p;
        return lvUp;
    }

    // Add money :v
    public void addMoney(int p) {
        this.money += p;
    }

    // Give player member role if all requirements satisfied
    public void checkMemberRole() {
        if (level >= 3) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                return;
            Member member = guild.getMemberById(userId);
            if (member == null) {
            } else {
                HashMap<String, Object> guildInfo = guildHandler.guildConfigs.get(guildId);
                if (guildInfo == null)
                    return;
                Object roleId = guildInfo.get("memberRole");
                if (roleId == null)
                    return;
                String _roleId = roleId.toString();
                if (_roleId == null || _roleId.isEmpty()) {
                    return;
                }
                Role memberRole = guild.getRoleById(_roleId);
                if (memberRole != null) {
                    guild.addRoleToMember(member, memberRole).queue();
                } else
                    System.out.println("Role not exist: " + roleId);
            }
        }
    }

    public AlphaUser mergeUser(AlphaUser data) {
        modify(guildId, userId, point, level, money + data.money, pvpPoint + data.pvpPoint, hideLevel);
        this.addPoint(data._getTotalPoint());
        return this;
    }
}
