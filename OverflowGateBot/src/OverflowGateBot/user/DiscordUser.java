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
    Integer money = 0;
    Boolean hideLv = false;

    public DiscordUser(@Nonnull String guildId, @Nonnull String id, String name, Integer point, Integer level, Integer money, Boolean hideLv) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.point = point;
        this.level = level;
        this.hideLv = hideLv;
        this.money = money;
    }

    public String toString() {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("GUILDID", guildId);
        map.put("NAME", name);
        map.put("POINT", point.toString());
        map.put("LEVEL", level.toString());
        map.put("NICKNAME", nickname);
        map.put("HIDELV", hideLv.toString());
        map.put("MONEY", money.toString());
        return new JSONObject(map).toJSONString();
    }

    public int getExpCap() {
        return level * level + 1;
    }

    public boolean addPoint(int p) {
        boolean lvUp = false;
        this.point += p;
        while (this.point >= this.getExpCap()) {
            this.point %= this.getExpCap();
            this.level += 1;
            lvUp = true;
            checkMemberRole();
        }
        return lvUp;
    }

    public void addMoney(int p) {
        this.money += p;
    }

    public void checkMemberRole() {
        if (level >= 3) {
            Guild guild = messagesHandler.jda.getGuildById(guildId);
            if (guild == null) {
                System.out.println("Guild not found: " + guildId);
                return;
            }
            Member member = guild.getMemberById(id);

            if (member != null) {
                String roleId = guildConfigHandler.memberRole.get(guildId);
                if (roleId == null || roleId.isEmpty())
                    return;
                Role memberRole = guild.getRoleById(roleId);
                if (memberRole != null)
                    guild.addRoleToMember(member, memberRole).queue();

            } else
                System.out.println("Not found " + getName());
        }
    }

    public String getName() {
        if (this.nickname.length() == 0) {
            return this.name;
        }
        return this.nickname;
    }

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

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getTotalPoint() {
        return (int) ((level + 1) * (2 * level + 1) * level / 6) + point;
    }
}
