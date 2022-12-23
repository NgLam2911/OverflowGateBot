package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;

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

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import static OverflowGateBot.OverflowGateBot.*;

public class LeaderboardCommand extends BotSubcommandClass {

    enum ORDER {
        LEVEL,
        MONEY,
        PVPPOINT
    }

    enum LEADERBOARD {
        GUILD,
        ALL
    }

    public LeaderboardCommand() {
        super("leaderboard", "Hiện thị bản xếp hạng của người dùng");
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
            throw new IllegalStateException("User left the guild");
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

        switch (leaderboard) {
            case ALL:
                jda.getGuilds().forEach(guild -> {
                    String guildId = guild.getId();
                    if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, guildId)) {
                        DatabaseHandler.userDatabase.createCollection(guildId);
                        DatabaseHandler.log(LOG_TYPE.DATABASE, "Create new user collection with guild id " + guildId);
                    }
                    MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(guildId,
                            UserData.class);

                    FindIterable<UserData> data = collection.find().//
                            sort(new Document().append("point", -1)).//
                            sort(new Document().append(order.name().toLowerCase(), -1));
                    data.forEach(d -> users.add(d));
                });
                break;

            case GUILD:
                Guild guild = event.getGuild();
                if (guild == null)
                    throw new IllegalStateException("Guild not found");
                String guildId = guild.getId();
                if (!DatabaseHandler.collectionExists(DatabaseHandler.userDatabase, guildId)) {
                    DatabaseHandler.userDatabase.createCollection(guildId);
                    DatabaseHandler.log(LOG_TYPE.DATABASE, "Create new user collection with guild id " + guildId);
                }
                MongoCollection<UserData> collection = DatabaseHandler.userDatabase.getCollection(guildId,
                        UserData.class);

                FindIterable<UserData> data = collection.find().sort(new Document().append("point", -1))
                        .sort(new Document().append(order.name().toLowerCase(), -1));
                data.forEach(d -> users.add(d));
                break;
        }
        switch (order) {
            case LEVEL:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b._getTotalPoint() - a._getTotalPoint();
                    }
                });

            case MONEY:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b.money - a.money;
                    }
                });
            case PVPPOINT:
                users.sort(new Comparator<UserData>() {
                    @Override
                    public int compare(UserData a, UserData b) {
                        return b.pvpPoint - a.point;
                    }
                });

        }

        EmbedBuilder builder = new EmbedBuilder();
        UserData user = userHandler.getUserAwait(member);
        int position = users.indexOf(user);

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
        String guildId = user.guildId;
        String userId = user.userId;
        if (guildId == null || userId == null)
            return data;
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return data;
        Member member = guild.getMemberById(userId);
        if (member == null)
            return data;
        data += member.getEffectiveName() + ":\t";
        switch (order) {
            case LEVEL:
                data += "cấp " + user.getLevel() + " (" + user._getTotalPoint() + " kinh nghiệm)";
                break;
            case MONEY:
                data += user.money + " Alpha";
                break;
            case PVPPOINT:
                data += user.pvpPoint + " điểm";
        }

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
