package OverflowGateBot.lib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import static OverflowGateBot.OverflowGateBot.*;

public class GuildData {

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
    public HashMap<CHANNEL_TYPE, List<String>> channelId = new HashMap<CHANNEL_TYPE, List<String>>();

    // Roles that require level to achieve
    public HashMap<String, Integer> levelRoleId = new HashMap<String, Integer>();

    public Guild guild;
    public HashMap<CHANNEL_TYPE, List<TextChannel>> channel = new HashMap<CHANNEL_TYPE, List<TextChannel>>();
    public HashMap<String, Role> levelRole = new HashMap<String, Role>();

    // For codec
    public GuildData() {
    }

    public GuildData(@Nonnull String guildId) {
        this.guildId = guildId;
        getGuild();
    }

    public void setShowLevel(boolean showLevel) {
        // No change, skip
        if (this.showLevel == showLevel)
            return;

        if (getGuild() == null)
            return;
        
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

    public void setChannelId(HashMap<CHANNEL_TYPE, List<String>> channelId) {
        this.channelId = channelId;
    }

    public HashMap<CHANNEL_TYPE, List<String>> getChannelId() {
        return this.channelId;
    }

    public void setLevelRoleId(HashMap<String, Integer> levelRoleId) {
        this.levelRoleId = levelRoleId;
    }

    public HashMap<String, Integer> getLevelRoleId() {
        return this.levelRoleId;
    }

    public Guild getGuild() {
        guild = jda.getGuildById(guildId);
        if (guild == null)
            throw new IllegalStateException("Guild with id " + guildId + " not found");
        return guild;
    }

    public boolean _isChannel(CHANNEL_TYPE channel_type, String channelId) {
        List<String> channelIds = this.channelId.get(channel_type);
        if (channelIds == null)
            return false;

        for (String c : channelIds)
            if (c.equals(channelId))
                return true;
        return false;
    }

    public List<TextChannel> _getChannel(CHANNEL_TYPE channel_type) {
        if (this.channel.containsKey(channel_type))
            return this.channel.get(channel_type);

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
        this.channel.put(channel_type, channels);
        return channels;
    }
}
