package OverflowGateBot.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BotSubcommandClass extends SubcommandData {

    private final int MAX_OPTIONS = 10;
    private final String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";

    public BotSubcommandClass(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    // Override
    public String getHelpString() {
        return "";
    }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        runCommand(event);
    }

    // Override
    protected void runCommand(SlashCommandInteractionEvent event) {
    }

    // Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

    }

    public void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    public void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    // Auto complete handler
    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, Set<String> input) {
        List<Command.Choice> options = new ArrayList<Command.Choice>();
        Set<String> list = new HashSet<String>();
        input.forEach(v -> list.add(v.replace(characterFilter, "")));

        if (list.isEmpty()) {
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        String focusString = event.getFocusedOption().getValue().toLowerCase();

        int count = 0;
        for (String value : list) {
            if (count > MAX_OPTIONS)
                break;
            if (value.toLowerCase().contains(focusString)) {
                options.add(new Command.Choice(value, value));
                count++;
            }
        }

        if (options.isEmpty()) {
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        event.replyChoices(options).queue();
    }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, String value) {
        if (value == null)
            event.replyChoice("Không tìm thấy kết quả khớp", "Không tìm thấy kết quả khớp").queue();
        else
            event.replyChoice(value, value).queue();
    }
}
