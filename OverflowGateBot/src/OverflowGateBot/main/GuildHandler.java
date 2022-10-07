package OverflowGateBot.main;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import OverflowGateBot.misc.JSONHandler;
import OverflowGateBot.misc.JSONHandler.JSONData;
import OverflowGateBot.misc.JSONHandler.JSONWriter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildHandler {

    public class ArchiveChannel {
        String channelId;
        String lastMessageId;

        public ArchiveChannel(String channelId, String lastMessageId) {
            this.channelId = channelId;
            this.lastMessageId = lastMessageId;
        }

        public ArchiveChannel() {

        }

        public TreeMap<String, String> toMap() {
            TreeMap<String, String> map = new TreeMap<>();
            map.put("channelId", channelId);
            map.put("lastMessageId", lastMessageId);
            return map;
        }
    }


    public class GuildConfig {
        // Channels
        public List<ArchiveChannel> guildChannel = new ArrayList<ArchiveChannel>();
        public List<ArchiveChannel> schematicChannel = new ArrayList<ArchiveChannel>();
        public List<ArchiveChannel> mapChannel = new ArrayList<ArchiveChannel>();

        public ArchiveChannel serverStatusChannel = new ArchiveChannel();
        public ArchiveChannel universeChatChannel = new ArchiveChannel();
        public ArchiveChannel botLog = new ArchiveChannel();

        // Roles
        public String adminRole;
        public String memberRole;

        public TreeMap<String, Object> toMap() {
            TreeMap<String, Object> map = new TreeMap<>();
            ArrayList<Object> list = new ArrayList<>();

            map.put("adminRole", adminRole);
            map.put("memberRole", memberRole);
            map.put("botLog", botLog.toMap());
            map.put("universeChatChannel", universeChatChannel.toMap());
            map.put("serverStatusChannel", serverStatusChannel.toMap());
            mapChannel.forEach(c -> list.add(c.toMap()));
            map.put("mapChannel", list);
            list.clear();
            schematicChannel.forEach(c -> list.add(c.toMap()));
            map.put("schematicChannel", list);
            list.clear();
            guildChannel.forEach(c -> list.add(c.toMap()));
            map.put("guildChannel", list);
            list.clear();
            return map;
        }
    }

    // Guilds
    public HashMap<String, GuildConfig> guildConfig = new HashMap<>();

    public final String adminName = "Sharlotte";


    public GuildHandler() {
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
            if (role.getId().equals(guildConfig.get(member.getId()).adminRole))
                return true;
        return false;
    }

    public boolean addGuild(String guildId) {
        if (guildConfig.keySet().contains(guildId))
            return false;
        guildConfig.put(guildId, new GuildConfig());
        save();
        return true;
    }

    public HashMap<String, String> getGuildsName() {
        HashMap<String, String> names = new HashMap<>();
        for (String guildId : guildConfig.keySet()) {
            if (guildId == null)
                break;
            Guild guild = messagesHandler.jda.getGuildById(guildId);
            if (guild == null)
                continue;
            names.put(guild.getName(), guildId);
        }
        return names;
    }

    public HashMap<String, String> getChannelsName(@Nonnull String guildId) {
        HashMap<String, String> names = new HashMap<>();
        Guild guild = messagesHandler.jda.getGuildById(guildId);
        if (guild == null) {
            System.out.println("Not found guild " + guildId);
            return names;
        }

        List<TextChannel> channels = guild.getTextChannels();
        for (TextChannel channel : channels) {
            names.put(channel.getName(), channel.getId());
        }
        return names;

    }

    public boolean inChannels(String channelId, String guildId, List<ArchiveChannel> channels) {
        for (ArchiveChannel channel : channels) {
            if (channel.channelId == channelId)
                return true;
        }
        return false;
    }

    public void setChannel(JSONData archiveChannel, String guildId, ArchiveChannel channel) {
        if (archiveChannel == null || archiveChannel.data == null)
            return;

        String channelId = archiveChannel.readString("channelId", null);
        if (channelId == null || channelId.equals("null"))
            return;

        String lastMessageId = archiveChannel.readString("lastMessageId", null);
        if (lastMessageId == null || lastMessageId.equals("null"))
            return;

        if (guildId == null)
            return;

        if (messagesHandler.hasChannel(guildId, channelId))
            channel = new ArchiveChannel(channelId, lastMessageId);
    }

    public void setChannel(SlashCommandInteractionEvent event, ArchiveChannel channel) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        channel = new ArchiveChannel(event.getChannel().getId(), null);
    }

    public void addChannel(SlashCommandInteractionEvent event, List<ArchiveChannel> channelList) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        addChannel(guild.getId(), event.getChannel().getId(), null, channelList);
    }

    public void addChannel(String guildId, JSONArray channelIds, List<ArchiveChannel> channelList) {
        for (Object channel : channelIds.toArray()) {
            try {
                ArchiveChannel c = (ArchiveChannel) channel;
                addChannel(guildId, c.channelId, c.lastMessageId, channelList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addChannel(String guildId, String channelId, String messageId, List<ArchiveChannel> channelList) {
        if (!inChannels(channelId, guildId, channelList))
            channelList.add(new ArchiveChannel(channelId, messageId));
    }

    public void setRole(String guildId, String newRoleId, String roleId) {
        roleId = newRoleId;
    }

    // TODO Database
    public void load() throws IOException {

        try {
            JSONHandler handler = new JSONHandler();
            JSONData reader = (handler.new JSONReader(guildFilePath)).read();
            if (reader.data == null)
                return;
            for (Object key : reader.data.keySet()) {
                String guildId = key.toString();
                if (!guildConfig.containsKey(guildId))
                    guildConfig.put(guildId, null);
                GuildConfig _guildConfig = guildConfig.get(guildId);

                JSONData guildData = reader.readJSON(guildId);
                JSONArray schematicChannels = guildData.readJSONArray("schematicChannel");
                addChannel(guildId, schematicChannels, _guildConfig.schematicChannel);
                JSONArray mapChannels = guildData.readJSONArray("mapChannel");
                addChannel(guildId, mapChannels, _guildConfig.mapChannel);
                JSONArray guildChannels = guildData.readJSONArray("guildChannel");
                addChannel(guildId, guildChannels, _guildConfig.guildChannel);
                JSONData serverStatusChannelId = guildData.readJSON("serverStatusChannel");
                setChannel(serverStatusChannelId, guildId, _guildConfig.serverStatusChannel);
                JSONData universeChatChannelId = guildData.readJSON("universeChatChannel");
                setChannel(universeChatChannelId, guildId, _guildConfig.universeChatChannel);

                String adminRoleId = guildData.readString("adminRole", null);
                setRole(guildId, adminRoleId, _guildConfig.adminRole);
                String memberRoleId = guildData.readString("memberRole", null);
                setRole(guildId, memberRoleId, _guildConfig.memberRole);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO Database
    public void save() {
        try {
            JSONHandler handler = new JSONHandler();
            JSONWriter writer = handler.new JSONWriter(guildFilePath);
            HashMap<String, Object> data = new HashMap<>();
            for (String guildId : guildConfig.keySet()) {
                data.put(guildId, guildConfig.get(guildId).toMap());
            }
            writer.write((new JSONObject(data)).toJSONString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

