package OverflowGateBot.main.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import OverflowGateBot.main.command.subcommands.UserCommands.LeaderboardCommand.LEADERBOARD;
import OverflowGateBot.main.command.subcommands.UserCommands.LeaderboardCommand.ORDER;
import OverflowGateBot.main.handler.DatabaseHandler;
import OverflowGateBot.main.handler.UserHandler;
import OverflowGateBot.main.handler.DatabaseHandler.DATABASE;
import OverflowGateBot.main.user.UserData;
import OverflowGateBot.main.util.SimpleTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

public class LeaderboardTable extends SimpleTable {

    private final LEADERBOARD leaderboard;
    private final ORDER order;
    private final int MAX_DISPLAY = 10;

    private List<UserData> users = new ArrayList<UserData>();

    public LeaderboardTable(@Nonnull SlashCommandInteractionEvent event, LEADERBOARD leaderboard, ORDER order) {
        super(event, 2);
        this.leaderboard = leaderboard;
        this.order = order;

        addButtonPrimary("<<<", () -> this.firstPage());
        addButtonPrimary("<", () -> this.previousPage());
        addButtonDeny("X", () -> this.delete());
        addButtonPrimary(">", () -> this.nextPage());
        addButtonPrimary(">>>", () -> this.lastPage());
    }

    @Override
    public @Nonnull MessageEmbed getCurrentPage() {
        if (users.size() <= 0)
            getLeaderboardData(this.leaderboard, this.order);

        return addPageFooter(getLeaderboard()).build();
    }

    @Override
    public void lastPage() {
        this.pageNumber = getMaxPage() - 1;
        updateTable();
    }

    @Override
    public int getMaxPage() { return (Integer) ((users.size() - 1) / MAX_DISPLAY) + 1; }

    public void getLeaderboardData(LEADERBOARD leaderboard, ORDER order) {

        switch (leaderboard) {
        case ALL:
            jda.getGuilds().forEach(guild -> {
                String guildId = guild.getId();
                if (!DatabaseHandler.collectionExists(DATABASE.USER, guildId))
                    DatabaseHandler.createCollection(DATABASE.USER, guildId);

                MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(guildId, UserData.class);

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

            MongoCollection<UserData> collection = DatabaseHandler.getDatabase(DATABASE.USER).getCollection(guildId, UserData.class);

            FindIterable<UserData> data = collection.find();
            data.forEach(d -> users.add(d));
            break;

        case ONLINE:
            users.addAll(UserHandler.getCachedUser());
        }
        switch (order) {
        case LEVEL:
            users.sort(new Comparator<UserData>() {
                @Override
                public int compare(UserData a, UserData b) { return b._getTotalPoint() - a._getTotalPoint(); }
            });
            break;

        case MONEY:
            users.sort(new Comparator<UserData>() {
                @Override
                public int compare(UserData a, UserData b) { return b.money - a.money; }
            });
            break;

        case PVP_POINT:
            users.sort(new Comparator<UserData>() {
                @Override
                public int compare(UserData a, UserData b) { return b.pvpPoint - a.point; }
            });
            break;

        default:
            users.sort(new Comparator<UserData>() {
                @Override
                public int compare(UserData a, UserData b) { return b._getTotalPoint() - a._getTotalPoint(); }
            });
            break;
        }
    }

    public EmbedBuilder getLeaderboard() {

        Member member = event.getMember();
        if (member == null)
            return new EmbedBuilder().addField("NULL", "L???i: Ng?????i d??ng kh??ng t???n t???i", false);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("B???NG X???P H???NG (" + leaderboard + ")");

        UserData user = UserHandler.getUserNoCache(member);
        int position = users.indexOf(user);

        int length = Math.min((pageNumber + 1) * MAX_DISPLAY, users.size());
        int start = pageNumber * MAX_DISPLAY;
        for (int i = start; i < length; i++)
            builder.addField("H???ng: " + (i + 1), getUserInformation(users.get(i), order), false);

        // Display sender position if its not contained in the leaderboard
        if (position <= pageNumber * MAX_DISPLAY || position > (pageNumber + 1) * MAX_DISPLAY)
            if (position >= 0)
                builder.addField("H???ng: " + (position + 1), getUserInformation(user, order), false);

        return builder;
    }

    public String getUserInformation(UserData user, ORDER order) {
        try {
            String data = "";
            data += user._getName() + ":               ";

            switch (order) {
            case LEVEL:
                data += "c???p " + user.getLevel() + " (" + user._getTotalPoint() + " kinh nghi???m)";
                break;
            case MONEY:
                data += user.money + " Alpha";
                break;
            case PVP_POINT:
                data += user.pvpPoint + " ??i???m";
            }
            data += "\nM??y ch???:           " + user._getGuild().getName();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return "Ng?????i d??ng ???? r???i kh???i m??y ch???";
        }
    }
}
