package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.BotException;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import static OverflowGateBot.OverflowGateBot.*;

public class DailyCommand extends BotSubcommandClass {
    public DailyCommand() {
        super("daily", "Điểm danh", true);
    }

    @Override
    public String getHelpString() {
        return "Điểm danh mỗi ngày";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException(BotException.GUILD_IS_NULL.getMessage());

        if (member == null)
            throw new IllegalStateException(BotException.MEMBER_IS_NULL.getMessage());

        if (DatabaseHandler.collectionExists(DATABASE.DAILY, guild.getId()))
            DatabaseHandler.getDatabase(DATABASE.GUILD).createCollection(guild.getId());

        MongoCollection<Document> collection = DatabaseHandler.getDatabase(DATABASE.DAILY).getCollection(guild.getId());

        Bson filter = new Document().append("userId", member.getId());
        FindIterable<Document> data = collection.find(filter).limit(1);
        UserData userData = userHandler.getUserAwait(member);

        int money = 0;
        int time = 0;
        if (data.first() == null) {
            money = userData._addMoney(userData._getLevelCap());
            collection.insertOne(new Document().append("userId", userData.userId).append(TIME_INSERT_STRING,
                    System.currentTimeMillis()));
        } else {
            if (System.currentTimeMillis() - time >= 86400000) { // 1 Day
                time = (int) data.first().get(TIME_INSERT_STRING);
                money = userData._addMoney(userData._getLevelCap());
                collection.replaceOne(filter,
                        new Document().append("userId", userData.userId).append(TIME_INSERT_STRING,
                                System.currentTimeMillis()));
            }
        }

        if (money > 0)
            reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + " Alpha\nĐiểm hiện tại: " + userData.money,
                    30);
        else {
            int sec = time / 1000;
            int minute = sec / 60;
            int hour = minute / 60;

            reply(event, "Còn " + hour % 24 + "h" + minute % 60 + " nữa mới có thể điểm danh\n Lần điểm danh cuối: "
                    + new BsonDateTime(time).asTimestamp(), 10);
        }
    }

}
