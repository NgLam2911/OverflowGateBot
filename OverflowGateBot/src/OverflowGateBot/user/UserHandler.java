package OverflowGateBot.user;


import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import OverflowGateBot.misc.JSONHandler;
import OverflowGateBot.misc.JSONHandler.JSONData;
import OverflowGateBot.misc.JSONHandler.JSONWriter;
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
    final String NAME = "n";
    final String NICKNAME = "nn";
    final String GUILDID = "g";
    final String HIDELV = "h";
    final String MONEY = "m";

    private HashMap<String, HashMap<String, DiscordUser>> users = new HashMap<>();
    private List<String> daily = new ArrayList<>();

    List<DiscordUser> board = new ArrayList<>();

    DataInputStream data;

    Clock clock = Clock.systemDefaultZone();

    Comparator<DiscordUser> sortByPoint = (o1, o2) -> o2.getTotalPoint().compareTo(o1.getTotalPoint());
    Comparator<DiscordUser> sortByMoney = (o1, o2) -> o2.money.compareTo(o1.money);

    HashMap<String, Comparator<DiscordUser>> sorter = new HashMap<>();

    public class DiscordUser {

        @Nonnull
        String id;
        @Nonnull
        String guildId;
        String name;
        String nickname = "";
        Integer point;
        Integer level;
        Integer money = 0;
        Boolean hideLv = false;

        public DiscordUser(@Nonnull String guildId, @Nonnull String id, String name, Integer point, Integer level, Integer money, Boolean hideLv) {
            this.id = id;
            this.guildId = guildId;
            this.name = name;
            this.point = point;
            this.level = level;
            this.hideLv = hideLv;
            this.money = money;
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
                Guild guild = messagesHandler.jda.getGuildById(guildId);
                if (guild == null) {
                    System.out.println("Guild not found: " + guildId);
                    return;
                }
                Member member = guild.getMemberById(id);

                if (member != null) {
                    String roleId = guildConfigHandler.memberRole.get(guildId);
                    if (roleId == null || roleId.isEmpty())
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
            Guild guild = messagesHandler.jda.getGuildById(guildId);
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
            Guild guild = messagesHandler.jda.getGuildById(guildId);
            if (guild == null) {
                System.out.println("Guild with id " + guildId + " not found");
                return;
            }
            Member member = guild.getMemberById(id);
            if (member == null) {
                System.out.println("Member with id " + id + " not found");
                return;
            }
            if (member.getUser().isBot())
                return;

            if (guildConfigHandler.isAdmin(member))
                return;

            if (member.getGuild().getSelfMember().canInteract(member)) {
                if (!member.getUser().isBot()) {
                    String name = getDisplayName();
                    if (name.length() == 0) {
                        System.out.println("Cant modify name of " + member.getEffectiveName());
                        return;
                    }
                    member.modifyNickname(name).queue();
                }
            } else if (!guildConfigHandler.isAdmin(member))
                System.out.println("Cant interact with " + member.getEffectiveName());
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Integer getTotalPoint() {
            return (int) ((level + 1) * (2 * level + 1) * level / 6) + point;
        }

        public int getPosition() {
            board.sort((o1, o2) -> o2.getTotalPoint().compareTo(o1.getTotalPoint()));
            return board.lastIndexOf(this) + 1;
        }
    }

    public UserHandler() {
        sorter.put("Money", sortByMoney);
        sorter.put("Level", sortByPoint);

        try {
            load();
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

    public DiscordUser addNewMember(@Nonnull String guildId, @Nonnull String id, String name, int point, int level, int money, Boolean hideLv) {
        DiscordUser user = new DiscordUser(guildId, id, name, point, level, money, hideLv);
        if (users.containsKey(guildId)) {
            // Already have guild id
            if (!users.get(guildId).containsKey(id)) {
                // User not found
                users.get(guildId).put(id, user);
                if (!board.contains(user)) {
                    board.add(user);
                }
            } else {
                return users.get(guildId).get(id);
            }
        } else {
            users.put(guildId, new HashMap<>());
            users.get(guildId).put(id, user);

            if (!board.contains(user)) {
                board.add(user);
            }
        }

        return user;

    }

    public String getDate() {
        return (new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime())).toString();
    }

    public void load() throws IOException {
        try {

            JSONHandler jsonHandler = new JSONHandler();

            // Load daily data
            JSONData reader = (jsonHandler.new JSONReader(dailyFilePath)).read();
            String date = reader.readString("date");

            if (date.equals(getDate())) {
                JSONArray dailyData = reader.readJSONArray("data");
                if (dailyData != null)
                    for (Object d : dailyData) {
                        String id = d.toString();
                        if (!daily.contains(id))
                            daily.add(id);
                    }
            } else
                System.out.println("New date");

            // Load user data

            reader = (jsonHandler.new JSONReader(userFilePath)).read();

            if (reader.size() != 0) {
                for (Object guildId : reader.data.keySet()) {
                    String gid = guildId.toString();
                    if (gid == null)
                        continue;

                    JSONData guildData = reader.readJSON(gid);
                    for (Object k : guildData.data.keySet()) {

                        String id = k.toString();
                        if (id == null)
                            continue;

                        JSONData userData = guildData.readJSON(id);
                        if (userData.data == null)
                            continue;
                        String name = userData.readString(NAME);
                        int point = userData.readInt(POINT);
                        int level = userData.readInt(LEVEL);
                        String nickname = userData.readString(NICKNAME);
                        Boolean hideLv = Boolean.parseBoolean(userData.readString(HIDELV));
                        int money = userData.readInt(MONEY);

                        Guild guild = messagesHandler.jda.getGuildById(gid);

                        if (guild == null)
                            continue;

                        if (name.length() == 0) {
                            Member member = guild.getMemberById(id);
                            if (member == null || member.getUser().isBot())
                                continue;
                            name = member.getUser().getName();
                        }

                        if (!guildConfigHandler.guildIds.contains(gid))
                            guildConfigHandler.guildIds.add(gid);

                        DiscordUser user = addNewMember(gid, id, name, point, level, money, hideLv);
                        if (level == 0)
                            System.out.println("\tUser error " + user.name);
                        user.setNickname(nickname);
                        user.checkMemberRole();
                    }
                }
            }
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        for (String gid : guildConfigHandler.guildIds) {

            if (gid == null)
                break;

            Guild guild = messagesHandler.jda.getGuildById(gid);
            if (guild == null) {
                System.out.println("Guild not found with id: " + gid);
                continue;
            }
            List<Member> members = guild.getMembers();
            for (Member m : members) {
                addNewMember(m);
                setDisplayName(m);
            }
        }
    }

    public void save() throws IOException {

        try {

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
        try {
            JSONHandler jsonHandler = new JSONHandler();
            JSONData reader;
            reader = (jsonHandler.new JSONReader(dailyFilePath)).read();
            String date = reader.readString("date");
            JSONWriter writer = jsonHandler.new JSONWriter(dailyFilePath);
            if (date.equals(getDate()))
                writer.append("data", daily.toString());
            else
                writer.append("data", (new ArrayList<String>()).toString());
            writer.append("date", "\"" + getDate() + "\"");
            writer.write();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void saveUserData() throws IOException {
        JSONHandler jsonHandler = new JSONHandler();
        JSONWriter writer = jsonHandler.new JSONWriter(userFilePath);
        for (String gid : users.keySet()) {
            writer.append(gid, new JSONObject(users.get(gid)).toJSONString());
        }
        writer.write();
    }

    public void messageSent(Message message) {
        Member member = message.getMember();
        if (member == null)
            return;
        DiscordUser user = getUser(member);
        if (user == null) {
            System.out.println("User " + member.getEffectiveName() + " not found");
            addNewMember(member);
            return;
        }
        user.addMoney(1);
        if (user.addPoint(1)) {
            setDisplayName(member);
        }
    }

    public void addMoney(Member member, int money) {
        DiscordUser user = getUser(member);
        if (user == null) {
            addNewMember(member);
            return;
        }
        user.addMoney(money);
    }

    public void setDisplayName(Member member) {
        DiscordUser user = getUser(member);
        if (user != null)
            user.setDisplayName();

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
        DiscordUser user = getUser(member);

        EmbedBuilder builder = new EmbedBuilder();
        if (user == null) {
            builder.setDescription("Không tìm thấy");
            return builder;
        }


        builder.setAuthor(user.name, null, member.getEffectiveAvatarUrl());
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
        builder.addField("**Kinh nghiệm: **", user.point + " \\ " + user.getExpCap(), false);
        builder.addField("**Tổng kinh nghiệm: **", user.getTotalPoint().toString(), false);
        builder.addField("**Điểm: **", user.money + " MM", false);
        builder.addField("**Hạng: **", user.getPosition() + " \\ " + board.size(), false);

        return builder;
    }


    public EmbedBuilder getLeaderBoard() {
        board.sort((o1, o2) -> o2.getTotalPoint().compareTo(o1.getTotalPoint()));
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bảng xếp hạng");
        for (int i = 0; i < (board.size() < 10 ? board.size() : 10); i++) {
            DiscordUser user = board.get(i);
            Guild guild = messagesHandler.jda.getGuildById(user.guildId);
            if (guild == null)
                builder.addField("Hạng " + (i + 1), (user.getName()) + ":\nKinh nghiệm: " + user.getTotalPoint() + "\nCấp: " + user.level, false);
            else
                builder.addField("Hạng " + (i + 1), "Tên: " + (user.getName()) + "\nKinh nghiệm: " + user.getTotalPoint() + "\nCấp: " + user.level + "\nMáy chủ: " + guild.getName(), false);
        }
        return builder;
    }
}