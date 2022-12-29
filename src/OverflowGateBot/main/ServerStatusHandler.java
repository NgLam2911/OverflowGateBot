package OverflowGateBot.main;

import java.awt.Color;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import mindustry.net.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import arc.util.Log;
import arc.util.Strings;

import static OverflowGateBot.OverflowGateBot.*;
import static mindustry.Vars.*;

public class ServerStatusHandler {

    final int SERVER_RELOAD_PEROID = 2 * 60 * 1000;

    HashMap<String, Integer> survivalMap = new HashMap<>();
    HashMap<String, HashMap<String, Message>> serverStatus = new HashMap<>();
    HashMap<String, String> servers = new HashMap<>();
    HashMap<String, String> serverStatusChannels = new HashMap<>();

    public ServerStatusHandler() {
        Net net = new Net(platform.getNet());

        net.discoverServers(this::addServerIP, this::endDiscover);

        // Load all servers for all guilds
        for (String guildId : serverStatusChannels.keySet()) {
            if (guildId == null)
                break;
            Guild guild = jda.getGuildById(guildId);
            if (guild == null)
                continue;
            String channelId = serverStatusChannels.get(guildId);
            if (channelId == null)
                continue;
            MessageChannel channel = guild.getTextChannelById(channelId);
            if (channel == null)
                continue;
            MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();

            List<Message> msg = history.getRetrievedHistory();
            HashMap<String, Message> guildServerStatus = new HashMap<String, Message>();

            msg.forEach(_msg -> {
                List<MessageEmbed> embed = _msg.getEmbeds();
                embed.forEach(_embed -> {
                    String title = _embed.getTitle();
                    if (title == null)
                        return;
                    else
                        title = title.replace("_", "");
                    boolean found = false;
                    for (String ip : servers.keySet()) {
                        if (ip.equals(title)) {
                            if (serverStatus.containsKey(guildId) && serverStatus.get(guildId).containsKey(ip))
                                break;

                            guildServerStatus.put(ip, _msg);
                            serverStatus.put(guildId, guildServerStatus);
                            found = true;
                            break;
                        }
                    }
                    if (found == false)
                        _msg.delete().queue();
                });
            });

            servers.keySet().forEach(ip -> displayServerStatus(guild, channel, ip));
        }

    }

    public void update() {

    }

    public void endDiscover() {
        Log.info("Done");
    }

    public void addServerIP(Host host) {
        servers.put(host.address, null);
    }

    public void displayServerStatus(Guild guild, MessageChannel channel, String ip) {
        networkHandler.run("AUTO REFRESH SERVER", 0l, SERVER_RELOAD_PEROID, () -> sendServerStatus(guild, channel, ip));
    }

    public void refreshServerStat(Guild guild, MessageChannel channel) {
        for (String ip : serverStatus.keySet()) {
            sendServerStatus(guild, channel, ip);
        }
    }

    private void sendServerStatus(Guild guild, MessageChannel channel, String ip) {
        networkHandler.pingServer(ip, result -> {
            EmbedBuilder builder = serverStatusBuilder(ip, result);
            if (serverStatus.containsKey(guild.getId()) && serverStatus.get(guild.getId()).containsKey(ip)) {
                serverStatus.get(guild.getId()).get(ip).editMessageEmbeds(builder.build()).queue();
            } else {
                HashMap<String, Message> guildServerStatus = new HashMap<String, Message>();
                if (!serverStatus.containsKey(guild.getId()))
                    serverStatus.put(guild.getId(), guildServerStatus);

                channel.sendMessageEmbeds(builder.build()).queue(_message -> {
                    guildServerStatus.put(ip, _message);
                    serverStatus.put(guild.getId(), guildServerStatus);
                });
            }
        });
    }

    public void reloadServer(Guild guild, @Nonnull MessageChannel channel) {

        MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();

        List<Message> msg = history.getRetrievedHistory();
        msg.forEach(_msg -> _msg.delete().queue());
        serverStatus.clear();
        refreshServerStat(guild, channel);
    }

    public EmbedBuilder serverStatusBuilder(String ip, Host result) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        builder.setTitle("__" + ip.toString() + "__");
        String owner = servers.get(ip);
        if (owner != null) {
            User user = jda.getUserById(owner);
            if (user != null)
                builder.setAuthor(user.getName(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
        }
        builder.setColor(Color.CYAN);

        if (result.name != null || result.mapname != null) {
            field.append("Tên máy chủ: " + Strings.stripColors(result.name) + "\nNgười chơi: " + result.players
                    + (result.playerLimit == 0 ? "" : " \\ " + result.playerLimit) + "\nBản đồ: "
                    + Strings.stripColors(result.mapname) + "\nChế độ: "
                    + (result.modeName == null ? messagesHandler.capitalize(result.mode.name())
                            : messagesHandler.capitalize(result.modeName))
                    + "\nĐợt: " + result.wave
                    + (result.description.length() == 0 ? "" : "\nMô tả: " + Strings.stripColors(result.description))
                    + "\nPhiên bản: "
                    + result.version + "\nPing: " + result.ping + "ms\n");

            String mapName = Strings.stripColors(result.mapname);
            if ((result.modeName != null && result.modeName.equals("Survival"))
                    || (result.mode.name().equals("survival"))) {
                if (!survivalMap.containsKey(mapName))
                    survivalMap.put(mapName, result.wave);
                else {
                    if (survivalMap.get(mapName) < result.wave)
                        survivalMap.put(mapName, result.wave);
                }
            }

        } else {
            field.append("Máy chủ không tồn tại hoặc ngoại tuyến\n");

        }
        builder.addField("Thông tin: ", field.toString(), true);
        builder.setFooter("Lần cập nhật cuối: "
                + Calendar.getInstance().getTime());
        return builder;
    }

    public int getWave(String mapName) {
        if (survivalMap.containsKey(mapName))
            return survivalMap.get(mapName);
        return 0;
    }

    public EmbedBuilder survivalMapLeaderboard() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("**Danh sách bản đồ:**");
        StringBuilder field = new StringBuilder();
        Map<String, Integer> mapSortedByKey = survivalMap.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for (String m : mapSortedByKey.keySet()) {
            field.append(m + ": " + survivalMap.get(m).toString() + "\n");
        }
        builder.addField("Kỷ lục đợt cao nhất của các bản đồ sinh tồn: ", field.toString(), false);
        return builder;
    }
}