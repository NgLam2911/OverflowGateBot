package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

public class DeletedMessageCommand extends BotSubcommandClass {

    private final int MAX_RETRIEVE = 10;

    public DeletedMessageCommand() {
        super("deletedmessage", "Hiển thị tin nhắn đã xóa gần đây nhất", true);
        this.addOption(OptionType.USER, "user", "Người xóa tin nhắn").//
                addOption(OptionType.INTEGER, "amount", "Số lượng tin nhắn");
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin người dùng:\n\t<user>: Tên người dùng muốn xem thông tin, nếu không nhập thì hiển thị thông tin bản thân";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping amountOption = event.getOption("amount");

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Document filter = new Document();
        if (userOption != null) {
            Member member = guild.getMember(userOption.getAsUser());
            if (member != null)
                filter.append("userId", member.getId());
        }
        int amount;
        if (amountOption == null)
            amount = MAX_RETRIEVE;
        else
            amount = amountOption.getAsInt();

        MongoCollection<Document> deletedCollection = DatabaseHandler.getDatabase(DATABASE.LOG)
                .getCollection(LOG_TYPE.MESSAGE_DELETED.name());
        MongoCollection<Document> messageCollection = DatabaseHandler.getDatabase(DATABASE.LOG)
                .getCollection(LOG_TYPE.MESSAGE.name());
        FindIterable<Document> data = deletedCollection.find().sort(new Document().append(TIME_INSERT_STRING, -1))
                .limit(amount);

        MongoCursor<Document> cursor = data.iterator();
        Document t;
        String name = "";
        EmbedBuilder builder = new EmbedBuilder();
        while (cursor.hasNext()) {
            t = cursor.next();
            if (t.containsKey("messageId"))
                filter.append("messageId", t.get("messageId"));
            Document message = messageCollection.find(filter).limit(1).first();
            if (message != null)
                if (message.containsKey("message")) {
                    if (message.containsKey("userId")) {
                        String content = message.get("userId").toString();
                        if (content != null) {
                            Member member = guild.getMemberById(content);
                            if (member != null) {
                                name = member.getEffectiveName();
                                builder.addField(name, message.get("message").toString(), false);
                            }
                        }
                    }
                }
            replyEmbeds(event, builder, 30);
        }
    }
}
