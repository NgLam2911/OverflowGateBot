package OverflowGateBot.main;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildHandler {

    // Guild config
    /*
     * public HashMap<String, String> guildChannel = new HashMap<>(); public
     * HashMap<String, String> schematicChannel = new HashMap<>(); public
     * HashMap<String, String> mapChannel = new HashMap<>(); public HashMap<String,
     * String> serverStatusChannel = new HashMap<>(); public HashMap<String, String>
     * universeChatChannel = new HashMap<>(); public HashMap<String, String> botLog
     * = new HashMap<>();
     */

    // Roles
    /*
     * public String adminRole; public String memberRole;
     */

    // Guilds
    public HashMap<String, HashMap<String, Object>> guildConfigs = new HashMap<>();
    public Set<String> guildIds = new HashSet<>();

    // For auto complete
    public Set<String> guildRoles = new HashSet<>();
    public Set<String> guildChannels = new HashSet<>();

    public HashMap<String, String> botLogChannels = new HashMap<>();

    public GuildHandler() {
        new File("cache/").mkdir();
        new File("cache/temp").mkdir();
        new File("cache/temp/map").mkdir();
        new File("cache/temp/schem").mkdir();
        new File("cache/data").mkdir();
        new File("cache/data/guild").mkdir();
        new File("cache/data/user").mkdir();

        guildRoles.add("adminRole");
        guildRoles.add("memberRole");

        guildChannels.add("channels");
        guildChannels.add("schematicChannel");
        guildChannels.add("mapChannel");
        guildChannels.add("botLog");
        guildChannels.add("universeChatChannel");
        guildChannels.add("serverStatusChannel");

        botLogChannels.put("1010373870395596830", "1010412857541799997");

        System.out.println("Guild handler up");
    }

    public boolean isAdmin(Message message) {
        return isAdmin(message.getMember());
    }

    public boolean isAdmin(Member member) {
        if (member.isOwner())
            return true;

        if (member.getUser().getId().equals(SHAR_ID))
            return true;

        HashMap<String, Object> guildConfig = guildConfigs.get(member.getGuild().getId());
        if (guildConfig == null)
            return false;

        for (Role role : member.getRoles())
            if (role.getId().equals(guildConfig.get("adminRole")))
                return true;
        return false;
    }
}
