package OverflowGateBot.main;


import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public final String adminId = "719322804549320725";
    public HashMap<String, HashMap<String, Object>> guildConfigs = new HashMap<>();
    public Set<String> guildIds = new HashSet<>();

    // For auto complete
    public Set<String> guildRoles = new HashSet<>();
    public Set<String> guildChannels = new HashSet<>();

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

        load();
    }

    public boolean isAdmin(Message message) {
        return isAdmin(message.getMember());
    }

    public boolean isAdmin(Member member) {
        if (member.isOwner())
            return true;

        if (member.getUser().getId().equals(adminId))
            return true;

        HashMap<String, Object> guildConfig = guildConfigs.get(member.getGuild().getId());
        if (guildConfig == null) return false;

        for (Role role : member.getRoles())
            if (role.getId().equals(guildConfig.get("adminRole")))
                return true;
        return false;
    }

    public boolean addGuild(String guildId) {
        if (guildIds.contains(guildId)) {
            return false;
        }
        guildIds.add(guildId);
        save();
        load();
        return true;
    }

    public HashMap<String, String> getAllGuildName() {
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

    public String getChannelName(@Nonnull Guild guild, HashMap<String, String> channels) {
        String result = "";
        for (String channelId : channels.keySet()) {
            if (channelId == null)
                continue;
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel == null)
                continue;
            result += channel.getName();
        }
        return result;
    }

    public String getRoleName(@Nonnull Guild guild, String roleId) {
        if (roleId == null)
            return "";
        Role role = guild.getRoleById(roleId);
        if (role == null)
            return "";
        return role.getName();
    }

    public HashMap<String, String> getAllGuildChannelName(@Nonnull String guildId) {
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

    public boolean inChannels(@Nonnull String guildId, @Nonnull String channelId, String channelName) {
        if (guildConfigs.containsKey(guildId))
            if (guildConfigs.get(guildId).containsKey(channelName)) {
                Object channelList = guildConfigs.get(guildId).get(channelName);
                if (channelList instanceof HashMap<?, ?>) {
                    HashMap<?, ?> hashChannelList = (HashMap<?, ?>) channelList;
                    for (Object _channelId : hashChannelList.keySet()) {
                        if (channelId == _channelId.toString())
                            return true;
                    }
                }
            }
        return false;
    }

    // TODO Database
    @SuppressWarnings("unchecked")
    public void load() {

        try {
            // Read all the guild id
            JSONHandler handler = new JSONHandler();
            JSONData reader = (handler.new JSONReader(guildFilePath)).read();
            if (reader.data == null)
                return;
            JSONArray _guildIds = reader.readJSONArray("guildIds");
            if (guildIds == null)
                return;
            for (Object _guildId : _guildIds) {
                guildIds.add(_guildId.toString());
            }

            HashMap<String, String> _temp = new HashMap<>();
            for (String _guildId : guildIds) {
                reader = (handler.new JSONReader(guildFilePath + _guildId)).read();
                // Continue if guild not registered yet
                if (!guildConfigs.containsKey(_guildId) || _guildId == null) {
                    guildConfigs.put(_guildId, new HashMap<String, Object>());
                    continue;
                }
                // Read all archive channels
                for (String channelType : guildChannels) {
                    if (reader.data.containsKey(channelType)) {
                        JSONData channels = reader.readJSON(channelType);
                        for (Object _channelId : channels.data.keySet()) {
                            String _channelLastMessage = channels.readString("messageId", null);
                            _temp.put(_channelId.toString(), _channelLastMessage);
                        }
                        guildConfigs.get(_guildId).put(channelType, _temp);
                        _temp.clear();
                    }
                }
                Guild guild = messagesHandler.jda.getGuildById(_guildId);
                if (guild == null)
                    continue;
                if (guildConfigs.get(_guildId).get("channels") == null)
                    guildConfigs.get(_guildId).put("channels", new HashMap<String, String>());
                for (TextChannel _channel : guild.getTextChannels()) {
                    if (!((HashMap<String, String>) guildConfigs.get(_guildId).get("channels")).containsKey(_channel.getId()))
                        ((HashMap<String, String>) guildConfigs.get(_guildId).get("channels")).put(_channel.getId(), null);
                }


                // Read all roles
                for (String roleType : guildRoles) {
                    if (!guildConfigs.containsKey(_guildId))
                        continue;
                    if (reader.data.containsKey(roleType)) {
                        String _roleId = reader.readString(roleType, null);
                        guildConfigs.get(_guildId).put(roleType, _roleId);
                    }
                }
            }


        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }

    // TODO Database
    @SuppressWarnings("unchecked")
    public void save() {
        try {
            JSONHandler handler = new JSONHandler();
            // Save all guild ids
            JSONWriter writer = handler.new JSONWriter(guildFilePath);
            JSONArray array = new JSONArray();
            for (String _guildId : guildIds) {
                array.add(_guildId);
            }
            JSONObject data = new JSONObject();
            data.put("guildIds", array);
            writer.write(data.toJSONString());

            // Write guild config into multiple file with name = guildId + guildData.json
            for (String _guildId : guildIds) {
                writer = handler.new JSONWriter(guildFilePath + _guildId);
                if (guildConfigs.get(_guildId) != null)
                    writer.write((new JSONObject(guildConfigs.get(_guildId))).toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

