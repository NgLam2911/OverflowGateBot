package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import static OverflowGateBot.OverflowGateBot.*;

public class LeaderboardCommand extends BotSubcommandClass {

    enum ORDER {
        LEVEL,
        MONEY,
        PVP_POINT
    }

    enum LEADERBOARD {
        GUILD,
        ONLINE,
        ALL
    }

    public LeaderboardCommand() {
        super("leaderboard", "Hiện thị bản xếp hạng của người dùng", true, true);
        this.addOptions(new OptionData(OptionType.STRING, "orderby", "Tên bảng xếp hạng", true, true)).//
                addOptions(new OptionData(OptionType.STRING, "leaderboard", "Tên bảng xếp hạng", true, true));
    }

    @Override
    public String getHelpString() {
        return "Hiện thị bản xếp hạng của người dùng:\n\t<orderby>: Xếp theo:\n\t\t- MONEY: Xếp theo tiền\n\t\t- LEVEL: Xếp theo cấp\n\t\t- PVP_POINT: Xếp theo điểm pvp";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("User not in guild");
        OptionMapping orderOption = event.getOption("orderby");
        OptionMapping leaderboardOption = event.getOption("leaderboard");
        ORDER order;
        LEADERBOARD leaderboard;

        // Default is sort by level
        if (orderOption == null)
            order = ORDER.LEVEL;
        else
            order = ORDER.valueOf(orderOption.getAsString());

        if (leaderboardOption == null)
            leaderboard = LEADERBOARD.GUILD;
        else
            leaderboard = LEADERBOARD.valueOf(leaderboardOption.getAsString());

        List<UserData> users = new ArrayList<UserData>();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("BẢNG XẾP HẠNG (" + leaderboard + ")");

        switch (leaderboard) {
            case ALL:
                jda.getGuilds().forEach(guild -> {
                    String guildId = guild.getId();
                    if (!DatabaseHandler.collectionExists(DATABASE.USER, guildId))
                        DatabaseHandler.createCollection(DATABASE.USER, guildId);

                    MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(
                            guildId,
                            UserData.class);

                    FindIterable<UserData> data = collection.find();
                    data.forEach(d -> users.add(d));
                });
                break;

            case GUILD:
                Guild guild = event.getGuild();
                if (guild == null)
                    throw new IllegalStateException("Guild not found");
                String guildId = guild.getId();
                if (!DatabaseHandler.collectionExists(DATABASE.USER, guildId))
                    DatabaseHandler.createCollection(DATABASE.USER, guildId);

                MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(guildId,
                        UserData.class);

                FindIterable<UserData> data = collection.find();
                data.forEach(d -> users.add(d));
                break;
            case ONLINE:
                users.addAll(userHandler.userCache.values());
        }
        switch (order) {
            case LEVEL:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b._getTotalPoint() - a._getTotalPoint();
                    }
                });
                break;

            case MONEY:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b.money - a.money;
                    }
                });
                break;

            case PVP_POINT:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b.pvpPoint - a.point;
                    }
                });
                break;

            default:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b._getTotalPoint() - a._getTotalPoint();
                    }
                });
                break;

        }

        UserData user = userHandler.getUserNoCache(member);
        int position = users.indexOf(user) + 1;

        // Display sender position if its not contained in the leaderboard
        int length = users.size() > 10 ? 10 : users.size();
        for (int i = 0; i < length; i++) {

            builder.addField("Hạng: " + (i + 1), getUserInformation(users.get(i), order), false);
        }
        if (position > 10)
            builder.addField("Hạng: " + position, getUserInformation(user, order), false);

        replyEmbeds(event, builder, 30);
    }

    public String getUserInformation(UserData user, ORDER order) {
        String data = "";
        data += user._getName() + ":               ";

        switch (order) {
            case LEVEL:
                data += "cấp " + user.getLevel() + " (" + user._getTotalPoint() + " kinh nghiệm)";
                break;
            case MONEY:
                data += user.money + " Alpha";
                break;
            case PVP_POINT:
                data += user.pvpPoint + " điểm";
        }
        data += "\nMáy chủ:           " + user._getGuild().getName();
        return data;
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("orderby")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (ORDER t : ORDER.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);

        } else if (focus.equals("leaderboard")) {
            HashMap<String, String> options = new HashMap<String, String>();
            for (LEADERBOARD t : LEADERBOARD.values())
                options.put(t.name(), t.name());
            sendAutoComplete(event, options);
        }
    }
}
