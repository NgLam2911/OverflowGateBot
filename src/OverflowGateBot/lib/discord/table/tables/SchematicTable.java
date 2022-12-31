package OverflowGateBot.lib.discord.table.tables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import OverflowGateBot.BotConfig;
import OverflowGateBot.lib.discord.table.SimpleTable;
import OverflowGateBot.lib.mindustry.ContentHandler;
import OverflowGateBot.lib.mindustry.SchematicData;
import OverflowGateBot.lib.mindustry.SchematicInfo;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.MessageHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import mindustry.game.Schematic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import static OverflowGateBot.OverflowGateBot.*;

public class SchematicTable extends SimpleTable {

    private List<SchematicInfo> schematicInfoList = new ArrayList<SchematicInfo>();
    private MongoCollection<SchematicData> collection;
    private SchematicInfo currentInfo;
    private SchematicData currentData;
    private Message currentCode;
    private List<String> voted = new ArrayList<String>();

    public SchematicTable(@Nonnull SlashCommandInteractionEvent event, FindIterable<SchematicInfo> schematicInfo) {
        super(event, 10);

        MongoCursor<SchematicInfo> cursor = schematicInfo.cursor();
        while (cursor.hasNext()) {
            schematicInfoList.add(cursor.next());
        }

        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_DATA_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, BotConfig.SCHEMATIC_DATA_COLLECTION);
        }
        this.collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY).getCollection(BotConfig.SCHEMATIC_DATA_COLLECTION, SchematicData.class);

        addButtonPrimary("<", () -> this.previousPage());
        addButtonDeny("X", () -> this.delete());
        addButtonPrimary(">", () -> this.nextPage());
        addButtonSuccess("data", Emoji.fromMarkdown("📁"), () -> this.sendCode());
        addButtonSuccess("star", Emoji.fromMarkdown("⭐"), () -> this.star());

    }

    @Override
    public int getMaxPage() { return this.schematicInfoList.size(); }

    @Override
    public void sendTable() { updateTable(); }

    @Override
    public void delete() {
        this.event.getHook().deleteOriginal().queue();
        if (this.currentCode != null)
            this.currentCode.delete().queue();
        this.killTimer();
    }

    public void star() {

        if (voted.contains(getTriggerMember().getId()))
            return;

        if (this.currentInfo != null) {
            this.currentInfo.star += 1;
            this.voted.add(getTriggerMember().getId());
            this.currentInfo.update();
            updateTable();
        }
    }

    public void sendCode() {
        if (this.currentData == null)
            return;
        String data = this.currentData.data;
        if (data == null)
            return;
        if (this.currentCode == null) {
            sendCodeData(data);
        } else {
            this.currentCode.delete().queue();
            sendCodeData(data);
        }
    }

    public void sendCodeData(@Nonnull String data) {
        if (this.currentData.data.length() < 1000)
            this.event.getHook().sendMessage("```" + data + "```").queue(m -> this.currentCode = m);
        else {
            try {
                File schematicFile = MessageHandler.getSchematicFile(ContentHandler.parseSchematic(data));
                this.event.getHook().sendFile(schematicFile).queue(m -> this.currentCode = m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateTable() {
        try {
            if (this.currentCode != null)
                this.currentCode.delete().queue();

            this.currentInfo = schematicInfoList.get(this.pageNumber);
            this.currentData = collection.find(Filters.eq("_id", currentInfo.id)).limit(1).first();
            if (this.currentData == null) {
                this.event.getHook().editOriginal("Không có dữ liệu về bản thiết kế với id:" + currentInfo.id).queue();
                return;
            }

            Schematic schem = ContentHandler.parseSchematic(this.currentData.getData());
            File previewFile = MessageHandler.getSchematicPreviewFile(schem);
            EmbedBuilder builder = MessageHandler.getSchematicEmbedBuilder(schem, previewFile, event.getMember());
            StringBuilder field = new StringBuilder();
            builder = addPageFooter(builder);
            String authorId = this.currentInfo.authorId;
            if (authorId != null) {
                User user = jda.getUserById(authorId);
                if (user != null)
                    field.append("- Tác giả: " + user.getName() + "\n");
            }

            field.append("- Nhãn: ");
            for (int i = 0; i < this.currentInfo.tag.size() - 1; i++)
                field.append(this.currentInfo.tag.get(i).toLowerCase() + ", ");
            field.append(this.currentInfo.tag.get(this.currentInfo.tag.size() - 1).toLowerCase() + "\n");
            field.append("- Sao: " + this.currentInfo.star);

            builder.addField("*Thông tin*", field.toString(), false);

            WebhookMessageUpdateAction<Message> action = this.event.getHook().editOriginal(previewFile);

            if (getTriggerMessage() != null)
                action.retainFiles(getTriggerMessage().getAttachments());

            action.setEmbeds(builder.build()).setActionRows(getButton()).queue();

        } catch (Exception e) {
            this.event.getHook().editOriginal("Lỗi").queue();
            e.printStackTrace();
        }
    }
}
