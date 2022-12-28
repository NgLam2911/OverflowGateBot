package OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import OverflowGateBot.lib.data.SCHEMATIC_TAG;
import OverflowGateBot.lib.data.SchematicInfo;
import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;
import OverflowGateBot.lib.discord.table.tables.SchematicTable;
import OverflowGateBot.main.BotException;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.DatabaseHandler.DATABASE;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

public class SearchSchematicCommand extends SimpleBotSubcommand {

    private final String SEPARATOR = ",";
    private final Integer SEARCH_LIMIT = 100;

    List<String> tags = SCHEMATIC_TAG.getTags();

    public SearchSchematicCommand() {
        super("searchschematic", "Tìm bản thiết kế dựa theo nhãn", true, false);
        this.addOption(OptionType.STRING, "tag", "Nhãn để lọc bản thiết kế", false, true)//
                .addOption(OptionType.USER, "user", "Tác giả của bản thiết kế");

    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {

        Document filter = new Document();
        OptionMapping tagOption = event.getOption("tag");
        String[] tags = {};
        if (tagOption != null) {
            tags = tagOption.getAsString().toUpperCase().split(SEPARATOR);
            // all("tag", tags);
        }

        OptionMapping userOption = event.getOption("user");
        if (userOption != null) {
            Member member = userOption.getAsMember();
            if (member != null)
                filter.append("authorId", member.getId());
        }

        if (!DatabaseHandler.collectionExists(DATABASE.MINDUSTRY, SCHEMATIC_INFO_COLLECTION)) {
            DatabaseHandler.createCollection(DATABASE.MINDUSTRY, SCHEMATIC_INFO_COLLECTION);
        }
        MongoCollection<SchematicInfo> collection = DatabaseHandler.getDatabase(DATABASE.MINDUSTRY).getCollection(
                SCHEMATIC_INFO_COLLECTION,
                SchematicInfo.class);

        FindIterable<SchematicInfo> schematicInfo = collection.find(Filters.and(Filters.all("tag", tags), filter),
                SchematicInfo.class).limit(SEARCH_LIMIT).sort(new Document().append("star", -1));

        if (schematicInfo.first() == null) {
            if (tagOption == null)
                reply(event, "Không có dữ liệu về bản thiết kế", 30);
            else
                reply(event, "Không có dữ liệu về bản thiết kế với nhãn: " + tagOption.getAsString().toLowerCase(), 30);
        } else {
            new SchematicTable(event, schematicInfo).send();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException(BotException.GUILD_IS_NULL.getMessage());

        String focus = event.getFocusedOption().getName();
        if (focus.equals("tag")) {
            OptionMapping tagOption = event.getOption("tag");
            if (tagOption == null)
                return;
            String tagValue = tagOption.getAsString().trim();
            String t = "";
            if (!tagValue.endsWith(SEPARATOR))
                t = tagValue.substring(tagValue.lastIndexOf(SEPARATOR) + 1, tagValue.length()).trim();

            List<String> temp = new ArrayList<String>(tags);
            List<String> tag = Arrays.asList(tagValue.split(SEPARATOR));
            temp.removeAll(tag);

            List<Command.Choice> options = new ArrayList<Command.Choice>();
            int c = 0;
            for (String i : temp) {
                if (i.startsWith(t.toUpperCase())) {
                    String value = tagValue.substring(0,
                            tagValue.lastIndexOf(SEPARATOR) + 1)
                            + i;
                    String display = value.toLowerCase();
                    options.add(new Command.Choice(display == null ? value : display, value));
                    c += 1;
                }
                if (c > MAX_OPTIONS)
                    break;
            }
            event.replyChoices(options).queue();
        }
    }
}
