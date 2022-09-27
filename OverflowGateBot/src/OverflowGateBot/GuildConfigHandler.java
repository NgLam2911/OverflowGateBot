package OverflowGateBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

public class GuildConfigHandler {

    public class ArchiveChannel {
        String channelId;
        String lastMessageId;

        public ArchiveChannel(String channelId, String lastMessageId) {
            this.channelId = channelId;
            this.lastMessageId = lastMessageId;
        }
    }

    public HashMap<String, List<ArchiveChannel>> schematicChannel = new HashMap<>();
    public HashMap<String, List<ArchiveChannel>> mapChannel = new HashMap<>();
    public HashMap<String, List<ArchiveChannel>> noTextChannel = new HashMap<>();
    public HashMap<String, ArchiveChannel> serverStatusChannel = new HashMap<>();
    public HashMap<String, List<String>> adminRoles = new HashMap<>();
    public HashMap<String, String> memberRoleId = new HashMap<>();
    public List<String> guildIds = new ArrayList<>();

    public final String adminName = "Shar";

    public GuildConfigHandler() {
        guildIds.add("1010373870395596830");
        memberRoleId.put("1010373870395596830", "1015997862619914291");
    }

    public boolean isAdmin(Message message) {
        return isAdmin(message.getMember());
    }

    public boolean isAdmin(Member member) {
        if (member.isOwner())
            return true;

        if (member.getEffectiveName().equals(adminName))
            return true;

        Guild guild = member.getGuild();
        List<String> guildAdminRoles = adminRoles.get(guild.getId());
        if (guildAdminRoles == null)
            return false;
        List<Role> memberRoles = member.getRoles();
        for (Role role : memberRoles) {
            for (String id : guildAdminRoles) {
                if (id == role.getId())
                    return true;
            }
        }
        return false;
    }
}
