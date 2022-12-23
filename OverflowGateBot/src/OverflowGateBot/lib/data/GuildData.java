package OverflowGateBot.lib.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildData extends DataCache {

    public enum CHANNEL_TYPE {
        SCHEMATIC,
        MAP,
        SERVER_STATUS,
        BOT_LOG
    }

    @Nonnull
    public String guildId = new String();

    public boolean showLevel = false;

    public List<String> adminRoleId = new ArrayList<String>();
    // Schematic channel id, map channel id
    public ConcurrentHashMap<String, List<String>> channelId = new ConcurrentHashMap<String, List<String>>();
    // Roles that require level to achieve
    public ConcurrentHashMap<String, Integer> levelRoleId = new ConcurrentHashMap<String, Integer>();

    private Guild guild;

    // For codec
    public GuildData() {
        super(GUILD_ALIVE_TIME, UPDATE_LIMIT);
    }

    public GuildData(@Nonnull String guildId) {
        super(GUILD_ALIVE_TIME, UPDATE_LIMIT);
        this.guildId = guildId;
        _getGuild();
    }

    public boolean setShowLevel(boolean showLevel) {
        if (this.showLevel == showLevel)
            return false;

        if (_getGuild() == null)
            throw new IllegalStateException("Guild id is invalid");

        Member bot = guild.getMember(jda.getSelfUser());
        if (bot == null)
            throw new IllegalStateException("Bot not in guild " + guildId);

        // Loop through guild members and modify their nickname
        guild.getMembers().forEach(member -> {
            if (member != null)
                if (bot.canInteract(member)) {
                    String name = member.getEffectiveName();
                    String nickname = name.substring(name.indexOf("]") + 1, 0);
                    member.modifyNickname(nickname).queue();
                }
        });
        this.showLevel = showLevel;
        update();
        return true;
    }

    public boolean getShowLevel() {
        return this.showLevel;
    }

    public void setAdminRoleId(List<String> adminRoleId) {
        this.adminRoleId = adminRoleId;
    }

    public List<String> getAdminRoleId() {
        return this.adminRoleId;
    }

    public void setChannelId(ConcurrentHashMap<String, List<String>> channelId) {
        this.channelId = channelId;
    }

    public ConcurrentHashMap<String, List<String>> getChannelId() {
        return this.channelId;
    }

    public void setLevelRoleId(ConcurrentHashMap<String, Integer> levelRoleId) {
        this.levelRoleId = levelRoleId;
    }

    public ConcurrentHashMap<String, Integer> getLevelRoleId() {
        return this.levelRoleId;
    }

    public Guild _getGuild() {
        Guild guild = jda.getGuildById(this.guildId);
        if (guild == null)
            throw new IllegalStateException("Guild not found with id <" + guildId + ">");
        return guild;
    }

    public boolean _containsChannel(String channel_type, String channelId) {
        List<String> channelIds = this.channelId.get(channel_type);
        if (channelIds == null)
            return false;
        return channelIds.contains(channelId);
    }

    public List<TextChannel> _getChannels(String channel_type) {
        List<String> channelIds = this.channelId.get(channel_type);
        if (channelIds == null)
            return null;

        TextChannel temp;
        List<TextChannel> channels = new ArrayList<TextChannel>();
        for (String c : channelIds) {
            if (c == null)
                continue;
            temp = guild.getTextChannelById(c);
            if (temp != null)
                channels.add(temp);
        }
        return channels;
    }

    public boolean _addChannel(String channel_type, String channel_id) {
        System.out.println("Command executed at addChannel");
        if (channelId.get(channel_type) == null)
            channelId.put(channel_type, new ArrayList<String>());

        if (channelId.get(channel_type).contains(channel_id))
            return false;
        boolean result = channelId.get(channel_type).add(channel_id);
        update();
        return result;

    }

    public boolean _removeChannel(String channel_type, String channel_id) {
        if (channelId.get(channel_type) == null)
            return false;
        boolean result = channelId.get(channel_type).remove(channel_id);
        update();
        return result;
    }

    public boolean _addRole(String roleId, int level) {
        boolean result = levelRoleId.put(roleId, level) == null;
        update();
        return result;
    }

    public boolean _removeRole(String roleId) {
        boolean result = levelRoleId.remove(roleId) != null;
        update();
        return result;
    }

    // Update guild on database
    @Override
    public void update() {
        // Create collection if it's not exist
        if (!DatabaseHandler.collectionExists(DatabaseHandler.guildDatabase, GUILD_COLLECTION)) {
            DatabaseHandler.guildDatabase.createCollection(GUILD_COLLECTION);
            DatabaseHandler.log(LOG_TYPE.DATABASE, "Create new guild collection with id: " + this.guildId);
        }
        MongoCollection<GuildData> collection = DatabaseHandler.guildDatabase.getCollection(GUILD_COLLECTION,
                GuildData.class);

        // Filter for guild id, guild id is unique for each collection
        Bson filter = new Document().append("guildId", this.guildId);
        collection.replaceOne(filter, this, new ReplaceOptions().upsert(true));
        DatabaseHandler.log(LOG_TYPE.DATABASE, "Update guild id: " + guildId);
    }

}
