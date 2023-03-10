package OverflowGateBot.main.command.subcommands.UserCommands;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import OverflowGateBot.BotConfig;
import OverflowGateBot.main.handler.BotException;
import OverflowGateBot.main.handler.DatabaseHandler;
import OverflowGateBot.main.handler.DatabaseHandler.DATABASE;
import OverflowGateBot.main.handler.DatabaseHandler.LOG_TYPE;
import OverflowGateBot.main.util.SimpleBotSubcommand;
import OverflowGateBot.main.util.SimpleTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DeletedMessageCommand extends SimpleBotSubcommand {

    private final int MAX_RETRIEVE = 100;
    private final int MAX_DISPLAY = 10;

    public DeletedMessageCommand() {
        super("deletedmessage", "Hiển thị tin nhắn đã xóa gần đây nhất", true, false);
        this.addOption(OptionType.INTEGER, "amount", "Số lượng tin nhắn", true).//
                addOption(OptionType.USER, "user", "Người xóa tin nhắn");
    }

    @Override
    public String getHelpString() { return "Hiển thị thông tin người dùng:\n\t<user>: Tên người dùng muốn xem thông tin, nếu không nhập thì hiển thị thông tin bản thân"; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping amountOption = event.getOption("amount");

        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalArgumentException(BotException.GUILD_IS_NULL.getMessage());

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
            amount = Math.min(amountOption.getAsInt(), MAX_RETRIEVE);

        MongoCollection<Document> deletedCollection = DatabaseHandler.getDatabase(DATABASE.LOG).getCollection(LOG_TYPE.MESSAGE_DELETED.name());

        MongoCollection<Document> messageCollection = DatabaseHandler.getDatabase(DATABASE.LOG).getCollection(LOG_TYPE.MESSAGE.name());

        FindIterable<Document> data = deletedCollection.find().sort(new Document().append(BotConfig.TIME_INSERT_STRING, -1));

        Document messageData;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        SimpleTable table = new SimpleTable(event, 2);
        table.addButtonPrimary("<<<", () -> table.firstPage())//
                .addButtonPrimary("<", () -> table.previousPage())//
                .addButtonDeny("X", () -> table.delete())//
                .addButtonPrimary(">", () -> table.nextPage())//
                .addButtonPrimary(">>>", () -> table.lastPage());

        MongoCursor<Document> cursor = data.iterator();
        int i = 0;
        while (cursor.hasNext()) {
            messageData = cursor.next();
            if (messageData.containsKey("messageId")) {
                filter.append("messageId", messageData.get("messageId"));
                Document message = messageCollection.find(filter).first();
                String guildId = getGuildId(message);
                if (!guild.getId().equals(guildId))
                    continue;
                String content = getMessage(message);
                if (content == null)
                    continue;
                field.append(content + "\n");
                if (i % MAX_DISPLAY == MAX_DISPLAY - 1) {
                    builder.addField("Tin nhắn đã xóa", field.toString(), false);
                    table.addPage(builder);
                    field = new StringBuilder();
                    builder.clear();
                }
                i += 1;

            }
            if (i >= amount)
                break;
        }
        builder.addField("Tin nhắn đã xóa", field.toString(), false);
        table.addPage(builder);
        table.sendTable();
    }

    public String getMessage(Document message) {
        if (message == null)
            return null;
        if (!message.containsKey("message"))
            return null;
        return message.get("message").toString();
    }

    public String getGuildId(Document message) {
        if (message == null)
            return null;
        if (!message.containsKey("guildId"))
            return null;
        return message.get("guildId").toString();
    }
}
