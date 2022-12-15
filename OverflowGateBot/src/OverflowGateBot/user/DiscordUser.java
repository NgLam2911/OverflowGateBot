package OverflowGateBot.user;

import java.util.HashMap;
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
    public String id;
    @Nonnull
    public String guildId;
    public String name;
    String nickname = "";
    public Integer point;
    public Integer level;
    public Integer money = 0;
    public Integer pvpPoint;
    public Boolean hideLv = false;

    public DiscordUser(@Nonnull String guildId, @Nonnull String id, String name, Integer point, Integer level,
            Integer money, Integer pvpPoint, Boolean hideLv) {
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

    // Get nickname
    public String getName() {
        if (this.nickname.length() == 0) {
            return this.name;
        }
        return this.nickname;
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

        if (hideLv)
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
                if (name.length() == 0) {

                    return;
                }
                member.modifyNickname(name).queue();
            }
        }
    }

    // Set nickname in database
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
