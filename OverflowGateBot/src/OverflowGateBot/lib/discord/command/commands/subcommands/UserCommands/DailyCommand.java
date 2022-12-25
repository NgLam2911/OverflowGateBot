package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.BotException;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

import java.text.DateFormat;
import java.util.Date;

import static OverflowGateBot.OverflowGateBot.*;

public class DailyCommand extends BotSubcommandClass {
    public DailyCommand() {
        super("daily", "Điểm danh", true, false);
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

        if (!DatabaseHandler.collectionExists(DATABASE.DAILY, guild.getId()))
            DatabaseHandler.createCollection(DATABASE.DAILY, guild.getId());

        MongoCollection<Document> collection = DatabaseHandler.getDatabase(DATABASE.DAILY).getCollection(guild.getId());

        Bson filter = new Document().append("userId", member.getId());
        Document data = collection.find(filter).limit(1).first();
        UserData userData = userHandler.getUserAwait(member);

        int money = 0;
        if (data == null || data.isEmpty()) {
            money = userData._addMoney(userData._getLevelCap());
            collection.insertOne(new Document().append("userId", userData.userId).append("time",
                    System.currentTimeMillis()));
        } else {
            if (data.containsKey("time")) {
                Long time = (Long) data.get("time");
                if (System.currentTimeMillis() - time >= 86400000l) { // 1 Day
                    money = userData._addMoney(userData._getLevelCap());
                    collection.replaceOne(filter,
                            new Document().append("userId", userData.userId).append("time",
                                    System.currentTimeMillis()));
                }
            }
        }
        if (money > 0)
            reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + " Alpha\nĐiểm hiện tại: " + userData.money,
                    30);
        else {
            if (data != null)
                if (data.containsKey("time")) {
                    Long lastTime = ((Long) data.get("time"));
                    Long time = lastTime + 86400000l - System.currentTimeMillis();
                    Long sec = time / 1000;
                    Long minute = sec / 60;
                    Long hour = minute / 60;
                    Date date = new Date(lastTime);
                    reply(event,
                            "Còn " + hour % 24 + " giờ " + minute % 60
                                    + " phút nữa mới có thể điểm danh\nLần điểm danh cuối: "
                                    + DateFormat.getInstance().format(date),
                            30);
                }
        }
    }

}
