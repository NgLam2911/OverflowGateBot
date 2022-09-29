package OverflowGateBot;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import OverflowGateBot.JSONHandler.JSONData;
import OverflowGateBot.JSONHandler.JSONWriter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildConfigHandler {

    public class ArchiveChannel {
        String channelId;
        String lastMessageId;

        public ArchiveChannel(String channelId, String lastMessageId) {
            this.channelId = channelId;
            this.lastMessageId = lastMessageId;
        }

        public String toString() {
            return "{channelId:\"" + channelId + "\", lastMessageId:\"" + lastMessageId + "\"}";
        }
    }

    // Channels
    public HashMap<String, List<ArchiveChannel>> schematicChannel = new HashMap<>();
    public HashMap<String, List<ArchiveChannel>> mapChannel = new HashMap<>();

    public HashMap<String, ArchiveChannel> serverStatusChannel = new HashMap<>();
    public HashMap<String, ArchiveChannel> universeChatChannel = new HashMap<>();

    // Roles
    public HashMap<String, String> adminRole = new HashMap<>();
    public HashMap<String, String> memberRole = new HashMap<>();

    // Guilds
    public List<String> guildIds = new ArrayList<>();

    public final String adminName = "Sharlotte";


    public GuildConfigHandler() {
        new File("cache/").mkdir();
        new File("cache/data").mkdir();
        new File("cache/temp").mkdir();

        try {
            load();
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAdmin(Message message) {
        return isAdmin(message.getMember());
    }

    public boolean isAdmin(Member member) {
        if (member.isOwner())
            return true;

        if (member.getUser().getName().equals(adminName))
            return true;

        for (Role role : member.getRoles())
            if (role.getId().equals(adminRole.get(member.getGuild().getId())))
                return true;
        return false;
    }

    public boolean inChannels(String channelId, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        List<ArchiveChannel> channels = channelIds.get(guildId);
        if (channels == null)
            return false;
        for (ArchiveChannel channel : channels) {
            if (channel.channelId == channelId)
                return true;
        }
        return false;
    }

    public void setChannel(JSONData archiveChannel, String guildId, HashMap<String, ArchiveChannel> channelIds) {
        String channelId = archiveChannel.readString("channelId");
        if (channelId.isEmpty())
            return;
        String lastMessageId = archiveChannel.readString("lastMessageId");
        channelIds.put(guildId, new ArchiveChannel(channelId, lastMessageId));
    }

    public void setChannel(SlashCommandInteractionEvent event, HashMap<String, ArchiveChannel> channelIds) {
        channelIds.put(event.getGuild().getId(), new ArchiveChannel(event.getChannel().getId(), null));
    }

    public void addToChannel(JSONData archiveChannel, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        String channelId = archiveChannel.readString("channelId");
        if (channelId.isEmpty())
            return;
        String lastMessageId = archiveChannel.readString("lastMessageId");
        addToChannel(channelId, lastMessageId, guildId, channelIds);
    }

    public void addToChannel(String channelId, String lastMessageId, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        if (channelIds.containsKey(guildId)) {
            if (inChannels(channelId, guildId, channelIds))
                return;
            else
                channelIds.get(guildId).add(new ArchiveChannel(channelId, lastMessageId));
        } else {
            channelIds.put(guildId, new ArrayList<ArchiveChannel>());
            channelIds.get(guildId).add(new ArchiveChannel(channelId, lastMessageId));
        }
    }

    public void addToChannel(JSONArray archiveChannels, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        for (Object ac : archiveChannels) {
            addToChannel((JSONData) ac, guildId, channelIds);
        }
    }

    public void addToChannel(SlashCommandInteractionEvent event, HashMap<String, List<ArchiveChannel>> channelIds) {
        addToChannel(event.getChannel().getId(), null, event.getGuild().getId(), channelIds);
    }

    public void setRole(String guildId, String roleId, HashMap<String, String> roleIds) {
        roleIds.put(guildId, roleId);
    }

    public void load() throws IOException {
        try {
            JSONHandler handler = new JSONHandler();
            JSONData reader = (handler.new JSONReader(guildFilePath)).read();

            for (Object key : reader.data.keySet()) {
                String guildId = key.toString();
                JSONData guildData = reader.readJSON(guildId);
                JSONArray schematicChannels = guildData.readJSONArray("schematicChannel");
                addToChannel(schematicChannels, guildId, schematicChannel);
                JSONArray mapChannels = guildData.readJSONArray("mapChannel");
                addToChannel(mapChannels, guildId, mapChannel);
                JSONData serverStatusChannelId = guildData.readJSON("serverStatusChannel");
                setChannel(serverStatusChannelId, guildId, serverStatusChannel);
                JSONData universeChatChannelId = guildData.readJSON("universeChatChannel");
                setChannel(universeChatChannelId, guildId, universeChatChannel);

                String adminRoleId = guildData.readString("adminRole");
                setRole(guildId, adminRoleId, adminRole);
                String memberRoleId = guildData.readString("memberRole");
                setRole(guildId, memberRoleId, memberRole);
            }

            for (Guild guild : messagesHandler.jda.getGuilds()) {
                if (!guildIds.contains(guild.getId()))
                    guildIds.add(guild.getId());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JSONHandler handler = new JSONHandler();
            JSONWriter writer = handler.new JSONWriter(guildFilePath);
            writer.append("schematicChannel", (new JSONObject(schematicChannel)).toJSONString());
            writer.append("mapChannel", (new JSONObject(mapChannel)).toJSONString());
            writer.append("serverStatusChannel", (new JSONObject(serverStatusChannel)).toJSONString());
            writer.append("universeChatChannel", (new JSONObject(universeChatChannel)).toJSONString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

