package OverflowGateBot.main.handler;

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
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import arc.util.Log;
import arc.util.Strings;

import static OverflowGateBot.OverflowGateBot.*;

public final class ServerStatusHandler {

    private static final int SERVER_RELOAD_PEROID = 2 * 60 * 1000;

    private static ServerStatusHandler instance = new ServerStatusHandler();

    private static HashMap<String, Integer> survivalMap = new HashMap<>();
    private static HashMap<String, HashMap<String, Message>> serverStatus = new HashMap<>();
    private static HashMap<String, String> servers = new HashMap<>();

    private ServerStatusHandler() {
        Net net = new Net(new ArcNetProvider());

        net.discoverServers(this::addServerIP, this::endDiscover);
    }

    public static ServerStatusHandler getInstance() { return instance; }

    public static void update() {}

    public void endDiscover() { Log.info("Done"); }

    public void addServerIP(Host host) { servers.put(host.address, null); }

    public static void displayServerStatus(Guild guild, MessageChannel channel, String ip) { UpdatableHandler.run("AUTO REFRESH SERVER", 0l, SERVER_RELOAD_PEROID, () -> sendServerStatus(guild, channel, ip)); }

    public static void refreshServerStat(Guild guild, MessageChannel channel) {
        for (String ip : serverStatus.keySet()) {
            sendServerStatus(guild, channel, ip);
        }
    }

    private static void sendServerStatus(Guild guild, MessageChannel channel, String ip) {
        NetworkHandler.pingServer(ip, result -> {
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

    public static void reloadServer(Guild guild, @Nonnull MessageChannel channel) {

        MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();

        List<Message> msg = history.getRetrievedHistory();
        msg.forEach(_msg -> _msg.delete().queue());
        serverStatus.clear();
        refreshServerStat(guild, channel);
    }

    public static EmbedBuilder serverStatusBuilder(String ip, Host result) {
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
            field.append("T??n m??y ch???: " + Strings.stripColors(result.name) + "\nNg?????i ch??i: " + result.players + (result.playerLimit == 0 ? "" : " \\ " + result.playerLimit) + "\nB???n ?????: " + Strings.stripColors(result.mapname) + "\nCh??? ?????: "
                    + (result.modeName == null ? MessageHandler.capitalize(result.mode.name()) : MessageHandler.capitalize(result.modeName)) + "\n?????t: " + result.wave + (result.description.length() == 0 ? "" : "\nM?? t???: " + Strings.stripColors(result.description)) + "\nPhi??n b???n: " + result.version
                    + "\nPing: " + result.ping + "ms\n");

            String mapName = Strings.stripColors(result.mapname);
            if ((result.modeName != null && result.modeName.equals("Survival")) || (result.mode.name().equals("survival"))) {
                if (!survivalMap.containsKey(mapName))
                    survivalMap.put(mapName, result.wave);
                else {
                    if (survivalMap.get(mapName) < result.wave)
                        survivalMap.put(mapName, result.wave);
                }
            }

        } else {
            field.append("M??y ch??? kh??ng t???n t???i ho???c ngo???i tuy???n\n");

        }
        builder.addField("Th??ng tin: ", field.toString(), true);
        builder.setFooter("L???n c???p nh???t cu???i: " + Calendar.getInstance().getTime());
        return builder;
    }

    public static int getWave(String mapName) {
        if (survivalMap.containsKey(mapName))
            return survivalMap.get(mapName);
        return 0;
    }

    public static EmbedBuilder survivalMapLeaderboard() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("**Danh s??ch b???n ?????:**");
        StringBuilder field = new StringBuilder();
        Map<String, Integer> mapSortedByKey = survivalMap.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for (String m : mapSortedByKey.keySet()) {
            field.append(m + ": " + survivalMap.get(m).toString() + "\n");
        }
        builder.addField("K??? l???c ?????t cao nh???t c???a c??c b???n ????? sinh t???n: ", field.toString(), false);
        return builder;
    }
}
