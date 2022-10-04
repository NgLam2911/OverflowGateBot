package OverflowGateBot.user;


import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.json.simple.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import static OverflowGateBot.OverflowGateBot.*;

public class DiscordUser {
    @Nonnull
    String id;
    @Nonnull
    String guildId;
    String name;
    String nickname = "";
    Integer point;
    Integer level;
    public Integer money = 0;
    Integer pvpPoint;
    Boolean hideLv = false;

    public DiscordUser(@Nonnull String guildId, @Nonnull String id, String name, Integer point, Integer level, Integer money, Integer pvpPoint, Boolean hideLv) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.point = point;
        this.level = level;
        this.hideLv = hideLv;
        this.money = money;
        this.pvpPoint = pvpPoint;
    }

    // To json string, use to store data in json file(Temporary)
    public String toString() {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("GUILDID", guildId);
        map.put("NAME", name);
        map.put("POINT", point.toString());
        map.put("LEVEL", level.toString());
        map.put("NICKNAME", nickname);
        map.put("HIDELV", hideLv.toString());
        map.put("MONEY", money.toString());
        map.put("PVPPOINT", pvpPoint.toString());
        return new JSONObject(map).toJSONString();
    }

    // Get level cap
    public int getExpCap() {
        return level * level;
    }

    // Add point for user
    public boolean addPoint(int p) {
        boolean lvUp = false;
        int extra;
        while (p > 0) {
            extra = p - this.getExpCap();
            if (extra >= 0) {
                this.level += 1;
                this.point += extra;
                lvUp = true;
                p -= this.getExpCap();
                checkMemberRole();
            } else {
                this.point += p;
                checkMemberRole();
                p = 0;
            }
        }

        while (point >= getExpCap()) {
            point -= getExpCap();
            level += 1;
            lvUp = true;
            checkMemberRole();
        }
        return lvUp;
    }

    // Add money :v
    public void addMoney(int p) {
        this.money += p;
    }

    // Give player member role if all requirements satisfied
    public void checkMemberRole() {
        if (level >= 3) {
            Guild guild = messagesHandler.jda.getGuildById(guildId);
            if (guild == null)
                return;
            Member member = guild.getMemberById(id);
            if (member == null) {
                System.out.println("Not found " + getName());
            } else {
                String roleId = guildConfigHandler.memberRole.get(guildId);
                if (roleId == null || roleId.isEmpty()) {
                    return;
                }
                Role memberRole = guild.getRoleById(roleId);
                if (memberRole != null) {
                    guild.addRoleToMember(member, memberRole).queue();
                } else
                    System.out.println("Role not exist: " + roleId);
            }
        }
    }

    // Get nickname
    public String getName() {
        if (this.nickname.length() == 0) {
            return this.name;
        }
        return this.nickname;
    }

    // Name with [Lv<Level>] <Nickname>
    public String getDisplayName() {
        Guild guild = messagesHandler.jda.getGuildById(guildId);
        if (guild == null) {
            System.out.println("Guild with id " + guildId + " not found");
            return "";
        }
        Member member = guild.getMemberById(id);
        if (member == null) {
            System.out.println("Member with id " + id + " not found");
            return "";
        }
        User user = member.getUser();

        if (hideLv)
            return (getName().length() == 0 ? user.getName() : getName());
        return "[Lv" + level + "] " + (getName().length() == 0 ? user.getName() : getName());
    }

    // Change user nickname to [Lv<Level>] <Nickname>
    public void setDisplayName() {
        Guild guild = messagesHandler.jda.getGuildById(guildId);
        if (guild == null) {
            System.out.println("Guild with id " + guildId + " not found");
            return;
        }
        Member member = guild.getMemberById(id);
        if (member == null) {
            System.out.println("Member with id " + id + " not found");
            return;
        }
        if (member.getUser().isBot())
            return;

        if (guildConfigHandler.isAdmin(member))
            return;

        if (member.getGuild().getSelfMember().canInteract(member)) {
            if (!member.getUser().isBot()) {
                String name = getDisplayName();
                if (name.length() == 0) {
                    System.out.println("Cant modify name of " + member.getEffectiveName());
                    return;
                }
                member.modifyNickname(name).queue();
            }
        } else if (!guildConfigHandler.isAdmin(member))
            System.out.println("Cant interact with " + member.getEffectiveName());
    }

    // Set nickname in database
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // Math!!!
    public Integer getTotalPoint() {
        return (int) ((level + 1) * (2 * level + 1) * level / 6) + point;
    }
}
