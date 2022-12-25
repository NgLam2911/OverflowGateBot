package OverflowGateBot.lib.discord.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.BotException;
import OverflowGateBot.lib.data.DataCache;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;

import static OverflowGateBot.OverflowGateBot.*;

public class TableEmbedMessageClass extends DataCache {

    private List<EmbedBuilder> tables = new ArrayList<EmbedBuilder>();
    private List<TableButton> buttons = new ArrayList<TableButton>();
    private int page = 0;
    private boolean showPageNumber = true;

    private final SlashCommandInteractionEvent event;

    public TableEmbedMessageClass(SlashCommandInteractionEvent event, int aliveLimit) {
        super(aliveLimit, 0);
        this.event = event;
    }

    public void finalize() {
        this.delete();
    }

    public Guild getGuild() {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException(BotException.GUILD_IS_NULL.name());
        return guild;
    }

    public TextChannel getTextChannel() {
        TextChannel channel = event.getTextChannel();
        return channel;
    }

    public String getId() {
        return this.event.getId();
    }

    public boolean addPage(EmbedBuilder value) {
        if (value.isEmpty())
            return false;
        return tables.add(value);
    }

    public boolean addButton(@Nonnull String buttonName, @Nonnull Runnable r) {
        Button button = Button.primary(getId() + ":" + buttonName, buttonName);
        TableButton newButton = new TableButton(button, r);
        if (buttons.contains(newButton))
            return false;
        return buttons.add(newButton);
    }

    public @Nonnull EmbedBuilder getCurrentPage() {
        EmbedBuilder value = this.tables.get(page);
        if (value == null)
            throw new IllegalStateException(BotException.TABLE_NO_CONTENT.name());
        if (showPageNumber)
            value.setFooter((page + 1) + "/" + (tables.size() + 1));
        return value;
    }

    public void nextPage() {
        this.page += 1;
        this.page %= this.tables.size();
        this.update();
    }

    public void previousPage() {
        this.page -= 1;
        if (this.page <= -1)
            this.page = this.tables.size() - 1;
        this.update();
    }

    public void firstPage() {
        this.page = 0;
        this.update();
    }

    public void lastPage() {
        this.page = this.tables.size() - 1;
        this.update();
    }

    public void delete() {
        this.event.getHook().deleteOriginal().queue();
        this.kill();
    }

    public void send() {

        WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(getCurrentPage().build());

        Collection<Button> temp = new ArrayList<Button>();
        for (TableButton key : buttons)
            temp.add(key.getButton());

        action.addActionRow(temp).queue();
        tableEmbedMessageHandler.add(this);

    }

    public void update(@Nonnull ButtonInteractionEvent event) {
        this.reset();
        event.getHook().editOriginalEmbeds(getCurrentPage().build()).queue();
    }

    public void onCommand(@Nonnull ButtonInteractionEvent event) {
        String key = event.getComponentId().split(":")[1];
        for (TableButton b : buttons) {
            String id = b.getId();
            if (id == null)
                continue;
            if (id.equals(key)) {
                b.getRunnable().run();
                update(event);
                break;
            }
        }
    }

    private class TableButton {
        private final Runnable r;
        private final Button button;

        public TableButton(@Nonnull Button button, @Nonnull Runnable r) {
            this.r = r;
            this.button = button;
        }

        public Runnable getRunnable() {
            return this.r;
        }

        public Button getButton() {
            return this.button;
        }

        public String getId() {
            return this.getButton().getId();
        }
    }

}
