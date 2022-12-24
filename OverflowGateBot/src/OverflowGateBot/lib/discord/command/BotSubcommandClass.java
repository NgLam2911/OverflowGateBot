package OverflowGateBot.lib.discord.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import static OverflowGateBot.OverflowGateBot.*;

public class BotSubcommandClass extends SubcommandData {

    private final int MAX_OPTIONS = 10;
    private boolean threaded = false;

    public BotSubcommandClass(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    public BotSubcommandClass(@Nonnull String name, @Nonnull String description, boolean threaded) {
        super(name, description);
        this.threaded = threaded;
    }

    // Override
    public String getHelpString() {
        return "";
    }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (this.threaded)
            networkHandler.run(name, 0, () -> runCommand(event));
        else
            runCommand(event);
    }

    // Override
    protected void runCommand(SlashCommandInteractionEvent event) {
    }

    // Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

    }

    // Auto complete handler
    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, HashMap<String, String> list) {
        if (list.isEmpty()) {
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        String focusString = event.getFocusedOption().getValue().toLowerCase();
        List<Command.Choice> options = new ArrayList<Command.Choice>();

        int count = 0;
        for (String name : list.keySet()) {
            if (count > MAX_OPTIONS)
                break;
            if (name.toLowerCase().contains(focusString)) {
                String value = list.get(name);
                if (value == null)
                    return;
                options.add(new Command.Choice(name, value));
                count++;
            }
        }

        if (options.isEmpty()) {
            sendAutoComplete(event, "Không tìm thấy kết quả khớp");
            return;
        }
        event.replyChoices(options).queue();
    }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, @Nonnull String value) {
        sendAutoComplete(event, value, value);
    }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, @Nonnull String name,
            @Nonnull String value) {
        if (value.isBlank())
            event.replyChoice("Không tìm thấy kết quả khớp", "Không tìm thấy kết quả khớp").queue();
        else
            event.replyChoice(name, value).queue();
    }

    public void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    public void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }
}
