package OverflowGateBot.user;

import java.util.HashMap;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import static OverflowGateBot.OverflowGateBot.*;

public class AlphaUser {
    @Nonnull
    public String id;
    @Nonnull
    public String guildId;
    public String name;
    public String nickname;
    public Integer point;
    public Integer level;
    public Integer money = 0;
    public Integer pvpPoint;
    public Boolean hideLevel = false;

    public AlphaUser(@Nonnull String guildId, @Nonnull String id, String name, Integer point, Integer level,
            Integer money, Integer pvpPoint, Boolean hideLevel) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.point = point;
        this.level = level;
        this.hideLevel = hideLevel;
        this.money = money;
        this.pvpPoint = pvpPoint;
    }

    public void setId(@Nonnull String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setGuildId(@Nonnull String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return this.guildId;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setHideLevel(boolean hideLevel) {
        this.hideLevel = hideLevel;
    }

    public boolean getHideLevel() {
        return this.hideLevel;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        if (this.nickname == null)
            return this.name;
        return this.nickname;
    }

    @Override
    public String toString() {
        return "id:" + this.id + "\n"
                + "guildId:" + this.guildId + "\n"
                + "name:" + this.name + "\n"
                + "nickname:" + this.nickname + "\n"
                + "point:" + this.point + "\n"
                + "level:" + this.level + "\n"
                + "hideLevel:" + this.hideLevel + "\n";
    }

    // Get level cap
    public int getLevelCap() {
        return level * level;
    }

    // Math!!!
    public Integer getTotalPoint() {
        return ((level - 1) * level * (2 * (level - 1) + 1) / 6) + point;
    }

    // Add point for user
    public boolean addPoint(int p) {
        boolean lvUp = false;
        int extra;
        while (point + p >= getLevelCap()) {
            extra = getLevelCap() - point;
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
            Member member = guild.getMemberById(id);
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

    // Name with [Lv<Level>] <Nickname>
    public String getDisplayName() {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return "";
        }
        Member member = guild.getMemberById(id);
        if (member == null) {
            return "";
        }
        User user = member.getUser();

        if (hideLevel)
            return (getName().length() == 0 ? user.getName() : getName());
        return "[Lv" + level + "] " + (getName().length() == 0 ? user.getName() : getName());
    }

    // Change user nickname to [Lv<Level>] <Nickname>
    public void setDisplayName() {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        Member member = guild.getMemberById(id);
        if (member == null) {
            return;
        }
        if (member.getUser().isBot())
            return;

        if (guildHandler.isAdmin(member))
            return;

        if (member.getGuild().getSelfMember().canInteract(member)) {
            if (!member.getUser().isBot()) {
                String name = getDisplayName();
                member.modifyNickname(name).queue();
            }
        }
    }
}
