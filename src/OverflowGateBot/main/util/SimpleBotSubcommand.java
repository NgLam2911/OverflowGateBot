package OverflowGateBot.main.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import OverflowGateBot.main.handler.UpdatableHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class SimpleBotSubcommand extends SubcommandData {

    protected final int MAX_OPTIONS = 10;
    private boolean threaded = false;
    private boolean updateMessage = false;

    public SimpleBotSubcommand(@Nonnull String name, @Nonnull String description) { super(name, description); }

    public SimpleBotSubcommand(@Nonnull String name, @Nonnull String description, boolean threaded, boolean updateMessage) {
        super(name, description);
        this.threaded = threaded;
        this.updateMessage = updateMessage;
    }

    // Override
    public String getHelpString() { return getDescription(); }

    // Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (updateMessage)
            reply(event, "Đang cập nhật", 60);
        if (this.threaded)
            UpdatableHandler.run(name, 0, () -> runCommand(event));
        else
            runCommand(event);
    }

    // Override
    protected void runCommand(SlashCommandInteractionEvent event) {}

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

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, @Nonnull String value) { sendAutoComplete(event, value, value); }

    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, @Nonnull String name, @Nonnull String value) {
        if (value.isBlank())
            event.replyChoice("Không tìm thấy kết quả khớp", "Không tìm thấy kết quả khớp").queue();
        else
            event.replyChoice(name, value).queue();
    }

    public void replyEmbed(SlashCommandInteractionEvent event, EmbedBuilder builder, int deleteAfter) { event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS)); }

    public void reply(SlashCommandInteractionEvent event, String content, int deleteAfter) { event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS)); }
}
