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

public class GuildConfigHandler {

    public class ArchiveChannel {
        String channelId;
        String lastMessageId;

        public ArchiveChannel(String channelId, String lastMessageId) {
            this.channelId = channelId;
            this.lastMessageId = lastMessageId;
        }

        public TreeMap<String, String> getJsonValue() {
            TreeMap<String, String> map = new TreeMap<>();
            map.put("channelId", channelId);
            map.put("lastMessageId", lastMessageId);
            return map;
        }
    }


    public class GuildConfig {

    }

    // Channels
    public HashMap<String, List<ArchiveChannel>> channels = new HashMap<String, List<ArchiveChannel>>();

    public HashMap<String, ArchiveChannel> schematicChannel = new HashMap<>();
    public HashMap<String, ArchiveChannel> mapChannel = new HashMap<>();
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

    public boolean addGuild(String guildId) {
        if (guildIds.contains(guildId))
            return false;
        guildIds.add(guildId);
        save();
        return true;
    }

    public HashMap<String, String> getGuildsName() {
        HashMap<String, String> names = new HashMap<>();
        for (String guildId : guildIds) {
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
        if (archiveChannel == null || archiveChannel.data == null)
            return;

        String channelId = archiveChannel.readString("channelId");
        if (channelId.equals("null"))
            return;

        String lastMessageId = archiveChannel.readString("lastMessageId");
        if (lastMessageId.equals("null"))
            return;

        if (guildId == null)
            return;

        if (messagesHandler.hasChannel(guildId, channelId))
            channelIds.put(guildId, new ArchiveChannel(channelId, lastMessageId));
    }

    public void setChannel(SlashCommandInteractionEvent event, HashMap<String, ArchiveChannel> channelIds) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        channelIds.put(guild.getId(), new ArchiveChannel(event.getChannel().getId(), null));
    }

    public void addToChannel(JSONData archiveChannel, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        if (archiveChannel == null || archiveChannel.data == null)
            return;
        String channelId = archiveChannel.readString("channelId");
        if (channelId.isEmpty())
            return;
        String lastMessageId = archiveChannel.readString("lastMessageId");
        if (guildId == null)
            return;

        if (messagesHandler.hasChannel(guildId, channelId))
            addToChannel(channelId, lastMessageId, guildId, channelIds);
    }

    public void addToChannel(String channelId, String lastMessageId, String guildId, HashMap<String, List<ArchiveChannel>> channelIds) {
        if (guildId == null || channelId == null)
            return;

        if (messagesHandler.hasChannel(guildId, channelId))

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
        if (archiveChannels == null)
            return;
        for (Object ac : archiveChannels) {
            addToChannel((JSONData) ac, guildId, channelIds);
        }
    }

    public void addToChannel(SlashCommandInteractionEvent event, HashMap<String, List<ArchiveChannel>> channelIds) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        addToChannel(event.getChannel().getId(), "", guild.getId(), channelIds);
    }

    public void setRole(String guildId, String roleId, HashMap<String, String> roleIds) {
        roleIds.put(guildId, roleId);
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
                if (!guildIds.contains(guildId))
                    guildIds.add(guildId);
                JSONData guildData = reader.readJSON(guildId);
                JSONData schematicChannels = guildData.readJSON("schematicChannel");
                setChannel(schematicChannels, guildId, schematicChannel);
                JSONData mapChannels = guildData.readJSON("mapChannel");
                setChannel(mapChannels, guildId, mapChannel);
                JSONData serverStatusChannelId = guildData.readJSON("serverStatusChannel");
                setChannel(serverStatusChannelId, guildId, serverStatusChannel);
                JSONData universeChatChannelId = guildData.readJSON("universeChatChannel");
                setChannel(universeChatChannelId, guildId, universeChatChannel);

                String adminRoleId = guildData.readString("adminRole");
                setRole(guildId, adminRoleId, adminRole);
                String memberRoleId = guildData.readString("memberRole");
                setRole(guildId, memberRoleId, memberRole);
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
            HashMap<String, HashMap<String, Object>> data = new HashMap<>();
            for (String guildId : guildIds) {
                HashMap<String, Object> sub = new HashMap<>();
                if (schematicChannel.containsKey(guildId))
                    sub.put("schematicChannel", schematicChannel.get(guildId).getJsonValue());

                if (mapChannel.containsKey(guildId))
                    sub.put("mapChannel", mapChannel.get(guildId).getJsonValue());

                if (serverStatusChannel.containsKey(guildId))
                    sub.put("serverStatusChannel", serverStatusChannel.get(guildId).getJsonValue());

                if (universeChatChannel.containsKey(guildId))
                    sub.put("universeChatChannel", universeChatChannel.get(guildId).getJsonValue());

                if (adminRole.containsKey(guildId))
                    sub.put("adminRole", adminRole.get(guildId));

                if (memberRole.containsKey(guildId))
                    sub.put("memberRole", memberRole.get(guildId));


                data.put(guildId, sub);
            }
            writer.write((new JSONObject(data)).toJSONString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

