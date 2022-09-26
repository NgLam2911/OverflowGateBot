package OverflowGateBot;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import OverflowGateBot.JSONHandler.JSONData;
import OverflowGateBot.JSONHandler.JSONWriter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    final String POINT = "p";
    final String LEVEL = "l";
    final String ROLEID = "r";
    final String NAME = "n";
    final String NICKNAME = "nn";
    final String GUILDID = "g";
    final String HIDELV = "h";
    final String MONEY = "m";

    public final String usersDataPath = "users.json";
    public final String guildsDataPath = "guilds.json";
    public final String dailyPath = "daily.json";

    private HashMap<String, HashMap<String, DiscordUser>> users = new HashMap<>();
    private HashMap<String, String> memberRoleId = new HashMap<>();
    private List<String> daily = new ArrayList<>();
    private List<String> guildIds = new ArrayList<>();

    List<DiscordUser> board = new ArrayList<>();

    DataInputStream data;

    Clock clock = Clock.systemDefaultZone();

    public class DiscordUser {

        String id;
        String guildId;
        String name;
        String nickname = "";
        Integer point;
        Integer level;
        Integer money = 0;
        Boolean hideLv = false;

        public DiscordUser(String guildId, String id, String name, Integer point, Integer level, Integer money, Boolean hideLv) {
            this.id = id;
            this.guildId = guildId;
            this.name = name;
            this.point = point;
            this.level = level;
            this.hideLv = hideLv;
        }

        public String toString() {
            TreeMap<String, String> map = new TreeMap<>();
            map.put(GUILDID, guildId);
            map.put(NAME, name);
            map.put(POINT, point.toString());
            map.put(LEVEL, level.toString());
            map.put(NICKNAME, nickname);
            map.put(HIDELV, hideLv.toString());
            map.put(MONEY, money.toString());
            return new JSONObject(map).toJSONString();
        }

        public int getExpCap() {
            return level * level + 1;
        }

        public boolean addPoint(int p) {
            boolean lvUp = false;
            this.point += p;
            while (this.point >= this.getExpCap()) {
                this.point %= this.getExpCap();
                this.level += 1;
                lvUp = true;
                checkMemberRole();
            }
            return lvUp;
        }

        public void addMoney(int p) {
            this.money += p;
        }

        public void checkMemberRole() {
            if (level >= 3) {
                Guild guild = messages.jda.getGuildById(guildId);
                if (guild == null) {
                    System.out.println("Guild not found: " + guildId);
                }
                Member member = guild.getMemberById(id);

                if (member != null) {
                    String roleId = memberRoleId.get(guildId);
                    if (roleId == null)
                        return;
                    Role memberRole = guild.getRoleById(roleId);
                    if (memberRole != null)
                        guild.addRoleToMember(member, memberRole).queue();

                } else
                    System.out.println("Not found " + getName());
            }
        }

        public String getName() {
            if (this.nickname.length() == 0) {
                return this.name;
            }
            return this.nickname;
        }

        public String getDisplayName() {
            Guild guild = messages.jda.getGuildById(guildId);
            if (guild == null) {
                System.out.println("Guild with id " + guildId + " not found");
                return "";
            }
            Member member = guild.getMemberById(id);
            if (member == null) {
                System.out.println("Member with id " + id + " not found");
                return "";
            }
            User user = member.getUser();

            if (hideLv)
                return (getName().length() == 0 ? user.getName() : getName());
            return "[Lv" + level + "] " + (getName().length() == 0 ? user.getName() : getName());
        }

        public void setDisplayName() {
            Guild guild = messages.jda.getGuildById(guildId);
            if (guild == null) {
                System.out.println("Guild with id " + guildId + " not found");
                return;
            }
            Member member = guild.getMemberById(id);
            if (member == null) {
                System.out.println("Member with id " + id + " not found");
                return;
            }
            if (member.getGuild().getSelfMember().canInteract(member)) {
                if (!member.getUser().isBot()) {
                    String name = getDisplayName();
                    if (name.length() == 0) {
                        System.out.println("Cant modify name of " + member.getEffectiveName());
                        return;
                    }
                    member.modifyNickname(name).queue();
                }
            } else
                System.out.println("Cant interact with " + member.getEffectiveName());
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Integer getTotalPoint() {
            return (int) ((level + 1) * (2 * level + 1) * level / 6) + point;
        }

        public Integer getPosition() {
            return board.indexOf(this);
        }
    }

    public UserHandler() {
        guildIds.add("1010373870395596830");
        memberRoleId.put("1010373870395596830", "1015997862619914291");
        try {
            load();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DiscordUser getUser(Member member) {
        if (users.containsKey(member.getGuild().getId()))
            return users.get(member.getGuild().getId()).get(member.getId());
        return null;
    }

    public void addNewMember(Member member) {
        if (member.getUser().isBot())
            return;
        addNewMember(member.getGuild().getId(), member.getId(), member.getUser().getName(), 0, 1, 0, false);
        setDisplayName(member);
    }

    public DiscordUser addNewMember(String guildId, String id, String name, int point, int level, int money, Boolean hideLv) {
        DiscordUser user = new DiscordUser(guildId, id, name, point, level, money, hideLv);
        if (users.containsKey(guildId)) {
            if (!users.get(guildId).containsKey(id)) {
                users.get(guildId).put(id, user);
            } else {
                return users.get(guildId).get(id);
            }
        } else {
            users.put(guildId, new HashMap<>());
            users.get(guildId).put(id, user);
        }
        if (!board.contains(user)) {
            board.add(user);
            sortLeaderBoard();
        }

        return user;

    }

    public String getDate() {
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())).toString();
    }

    public void load() throws IOException {
        try {

            JSONHandler jsonHandler = new JSONHandler();
            // Load guild ids
            File file = new File("cache/data/" + guildsDataPath);

            JSONData reader = (jsonHandler.new JSONReader("cache/data/" + guildsDataPath)).read();
            for (Object k : reader.data.keySet()) {
                String guildId = k.toString();
                if (!guildIds.contains(guildId))
                    guildIds.add(guildId);
            }

            // Load daily data
            file = new File("cache/data/" + dailyPath);
            reader = (jsonHandler.new JSONReader("cache/data/" + dailyPath)).read();
            String date = reader.readString("date");

            if (date.equals(getDate())) {
                JSONArray dailyData = reader.readJSONArray(dailyPath);
                for (Object d : dailyData) {
                    String id = d.toString();
                    if (!daily.contains(id))
                        daily.add(id);
                }
            }

            // Load user data
            new File("cache/").mkdir();

            file = new File("cache/data/" + usersDataPath);
            if (!file.exists())
                file.createNewFile();

            reader = (jsonHandler.new JSONReader("cache/data/" + usersDataPath)).read();

            if (reader.size() != 0) {
                for (Object guildId : reader.data.keySet()) {
                    String gid = guildId.toString();
                    JSONData guildData = reader.readJSON(gid);
                    for (Object k : guildData.data.keySet()) {

                        String id = k.toString();
                        JSONData data = guildData.readJSON(id);
                        if (data.data == null)
                            continue;
                        String name = data.readString(NAME);
                        int point = data.readInt(POINT);
                        int level = data.readInt(LEVEL);
                        String nickname = data.readString(NICKNAME);
                        Boolean hideLv = Boolean.parseBoolean(data.readString(HIDELV));
                        int money = data.readInt(MONEY);

                        Guild guild = messages.jda.getGuildById(gid);

                        if (name.length() == 0 || name.equals("")) {
                            Member member = guild.getMemberById(id.toString());
                            if (member == null || member.getUser().isBot())
                                continue;
                            name = member.getUser().getName();
                        }

                        if (!guildIds.contains(gid))
                            guildIds.add(gid);

                        DiscordUser user = addNewMember(gid, id, name, point, level, money, hideLv);
                        user.setNickname(nickname);
                        user.checkMemberRole();
                    }
                }
            }
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        for (String gid : guildIds) {

            Guild guild = messages.jda.getGuildById(gid);
            if (guild == null) {
                System.out.println("Guild not found with id: " + gid);
                continue;
            }
            List<Member> members = guild.getMembers();
            for (Member m : members) {
                addNewMember(m);
                System.out.println("Loaded user: " + m.getEffectiveName());
                setDisplayName(m);

            }
        }
    }

    public void save() throws IOException {

        try {
            // Save guild ids
            saveGuildData();

            // Save daily data
            saveDailyData();

            // Save user data
            saveUserData();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error when saving user data");
        }
    }

    public void saveDailyData() throws IOException {
        JSONHandler jsonHandler = new JSONHandler();
        JSONWriter writer = jsonHandler.new JSONWriter("cache/data/" + dailyPath);
        writer.append(dailyPath, daily.toString());
        writer.write();
    }

    public void saveGuildData() throws IOException {
        JSONHandler jsonHandler = new JSONHandler();
        // Save guild ids
        JSONWriter writer = jsonHandler.new JSONWriter("cache/data/" + guildsDataPath);
        for (String k : guildIds) {
            writer.append(k, k);
        }
        writer.write();
    }

    public void saveUserData() throws IOException {
        JSONHandler jsonHandler = new JSONHandler();
        JSONWriter writer = jsonHandler.new JSONWriter("cache/data/" + usersDataPath);
        for (String gid : users.keySet()) {
            writer.append(gid, new JSONObject(users.get(gid)).toJSONString());
        }
        writer.write();
    }

    public void messageSent(Message message) {
        Member member = message.getMember();
        DiscordUser user = getUser(member);
        if (user == null) {
            System.out.println("User " + member.getEffectiveName() + " not found");
            addNewMember(member);
            return;
        }
        if (user.addPoint(1)) {
            setDisplayName(member);
        }
    }

    public void setDisplayName(Member member) {
        if (member.getUser().isBot())
            return;

        if (member.getGuild().getSelfMember().canInteract(member)) {
            if (!member.getUser().isBot()) {
                DiscordUser user = getUser(member);
                String name = user.getDisplayName();

                if (name.length() == 0) {
                    System.out.println("Cant modify name of " + member.getEffectiveName());
                    return;
                }
                member.modifyNickname(name).queue();
            }
        } else
            System.out.println("Cant interact with " + member.getEffectiveName());
    }

    public void setNickName(Member member, String nickname) {
        DiscordUser user = getUser(member);
        if (user == null)
            return;
        user.setNickname(nickname);
        setDisplayName(member);
    }

    public void hidelv(Member member, Boolean hide) {
        DiscordUser user = getUser(member);
        if (user == null)
            return;
        user.hideLv = hide;
        setDisplayName(member);
    }

    public int getDaily(Member member) {
        DiscordUser user = getUser(member);
        if (user == null)
            return -1;
        if (daily.contains(user.id))
            return -1;
        daily.add(user.id);
        int money = user.getExpCap();
        user.addMoney(money);
        return money;
    }

    public EmbedBuilder getInfo(Member member, TextChannel channel) {
        addNewMember(member);
        String userId = member.getId();
        DiscordUser user = getUser(member);

        EmbedBuilder builder = new EmbedBuilder();
        if (user == null) {
            builder.setDescription("Không tìm thấy");
            return builder;
        }
        builder.setAuthor(user.name, null, channel.getGuild().getMemberById(userId).getEffectiveAvatarUrl());
        List<Role> roles = member.getRoles();
        if (roles.size() != 0) {
            String r = "";
            for (Role rn : roles) {
                r += ", " + rn.getName();
            }
            r = r.substring(1);
            builder.addField("**Vai trò: **", r, false);
        }

        builder.addField("**Cấp: **", user.level.toString(), false);
        builder.addField("**Kinh nghiệm: **", user.point.toString() + " \\ " + user.getExpCap(), false);
        builder.addField("**Tổng kinh nghiệm: **", user.getTotalPoint().toString(), false);
        builder.addField("**Điểm: **", user.money.toString() + " MM", false);
        builder.addField("**Hạng: **", user.getPosition().toString() + " \\ " + board.size(), false);
        builder.addBlankField(false);

        return builder;
    }

    public void sortLeaderBoard() {
        for (int i = 0; i < board.size() - 1; i++) {
            for (int j = i + 1; j < board.size(); j++) {
                DiscordUser f = board.get(i);
                DiscordUser e = board.get(j);
                if (f.getTotalPoint() < e.getTotalPoint()) {
                    board.set(i, e);
                    board.set(j, f);
                }
            }
        }
    }

    public EmbedBuilder getLeaderBoard() {
        EmbedBuilder builder = new EmbedBuilder();
        sortLeaderBoard();
        builder.setTitle("Bảng xếp hạng");
        for (int i = 0; i < (board.size() < 10 ? board.size() : 10); i++) {
            DiscordUser user = board.get(i);
            Guild guild = messages.jda.getGuildById(user.guildId);
            if (guild == null)
                builder.addField("Hạng " + (i + 1), (user.getName()) + ": " + user.getTotalPoint(), false);
            else
                builder.addField("Hạng " + (i + 1), (user.getName()) + ":\n﻿Kinh nghiệm: " + user.getTotalPoint() + "\n﻿Cấp: " + user.level + "\n﻿Máy chủ: " + guild.getName(), false);
        }
        return builder;
    }
}