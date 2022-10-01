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

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    private HashMap<String, HashMap<String, DiscordUser>> users = new HashMap<>();
    private List<String> daily = new ArrayList<>();

    List<DiscordUser> board = new ArrayList<>();

    DataInputStream data;

    Clock clock = Clock.systemDefaultZone();

    Comparator<DiscordUser> sortByPoint = (o1, o2) -> o2.getTotalPoint().compareTo(o1.getTotalPoint());
    Comparator<DiscordUser> sortByMoney = (o1, o2) -> o2.money.compareTo(o1.money);

    public HashMap<String, Comparator<DiscordUser>> sorter = new HashMap<>();

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


    // Get user rank base on <orderBy>
    public int getPosition(DiscordUser user, String orderBy) {
        Comparator<DiscordUser> comparator = sorter.get(orderBy);
        if (comparator == null) {
            comparator = sortByPoint;
        }
        board.sort(comparator);
        return board.lastIndexOf(user) + 1;
    }

    // Get DiscordUser from Member, return null if not found
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
                // User already exists, return user
                // Add user to leaderboard
                if (!board.contains(user)) {
                    board.add(user);
                }
                return users.get(guildId).get(id);
            }
        } else {
            // Guild and user not exists, add to database
            users.put(guildId, new HashMap<>());
            users.get(guildId).put(id, user);
            // Add user to leaderboard
            if (!board.contains(user)) {
                board.add(user);
            }
        }

        return user;

    }

    // Get date for daily command
    public String getDate() {
        return (new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime())).toString();
    }

    // Update point, money, level on massage sent
    public void onMessage(Message message) {
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

    // Add money for member
    public void addMoney(Member member, int money) {
        DiscordUser user = getUser(member);
        if (user == null) {
            addNewMember(member);
            return;
        }
        user.addMoney(money);
    }

    // Set name to [Lv<Level>] <Nickname>
    public void setDisplayName(Member member) {
        DiscordUser user = getUser(member);
        if (user != null)
            user.setDisplayName();

    }

    // Set user nickname and save it
    public void setNickName(Member member, String nickname) {
        DiscordUser user = getUser(member);
        if (user == null)
            return;
        user.setNickname(nickname);
        setDisplayName(member);
    }

    // Hide member level, affect setDisplayName
    public void hidelv(Member member, Boolean hide) {
        DiscordUser user = getUser(member);
        if (user == null)
            return;
        user.hideLv = hide;
        setDisplayName(member);
    }

    // Get daily reward
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

    // Display roles, levels, points, and money
    public EmbedBuilder getUserInfo(Member member) {
        DiscordUser user = getUser(member);

        EmbedBuilder builder = new EmbedBuilder();
        if (user == null) {
            builder.setDescription("Không tìm thấy");
            return builder;
        }

        builder.setAuthor(user.name, null, member.getEffectiveAvatarUrl());
        List<Role> roles = member.getRoles();
        if (roles.size() != 0) {
            List<String> roleList = new ArrayList<String>();
            for (Role role : roles) {
                roleList.add(role.getName());
            }
            builder.addField("**Vai trò: **", roleList.toString(), false);
        }

        builder.addField("**Cấp: **", user.level.toString(), false);
        builder.addField("**Kinh nghiệm: **", user.point + " \\ " + user.getExpCap(), false);
        builder.addField("**Tổng kinh nghiệm: **", user.getTotalPoint().toString(), false);
        builder.addField("**Điểm: **", user.money + " MM", false);

        return builder;
    }

    public String getUserStat(DiscordUser user, String stat) {
        Guild guild = messagesHandler.jda.getGuildById(user.guildId);
        String guildName = "Unknown guild";
        if (guild != null)
            guildName = guild.getName();

        String displayedStat = "Cấp: " + user.level + "\nKinh nghiệm: " + user.getTotalPoint();
        switch (stat) {
        case "Money":
            displayedStat = "Điểm: " + user.money;
            break;
        }

        return "Tên: " + (user.getName()) + "\n" + displayedStat + "\nMáy chủ: " + guildName;
    }

    // Leaderboard base on <orderBy>
    public EmbedBuilder getLeaderBoard(String orderBy) {
        Comparator<DiscordUser> comparator = sorter.get(orderBy);
        if (comparator == null) {
            comparator = sortByPoint;
        }
        // Sort the leaderboard by <orderBy>, default is sort by point
        board.sort(comparator);
        // Build embed
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bảng xếp hạng");
        for (int i = 0; i < (board.size() < 10 ? board.size() : 10); i++) {
            DiscordUser user = board.get(i);
            builder.addField("Hạng " + (i + 1), getUserStat(user, orderBy), false);
        }

        return builder;
    }

    // TODO Database
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
                        String name = userData.readString("NAME");
                        int point = userData.readInt("POINT");
                        int level = userData.readInt("LEVEL");
                        String nickname = userData.readString("NICKNAME");
                        Boolean hideLv = Boolean.parseBoolean(userData.readString("HIDELV"));
                        int money = userData.readInt("MONEY");

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

    // Save and load
    // TODO Database
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

    // TODO Database
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

    // TODO Database
    public void saveUserData() throws IOException {
        JSONHandler jsonHandler = new JSONHandler();
        JSONWriter writer = jsonHandler.new JSONWriter(userFilePath);
        for (String gid : users.keySet()) {
            writer.append(gid, new JSONObject(users.get(gid)).toJSONString());
        }
        writer.write();
    }
}
