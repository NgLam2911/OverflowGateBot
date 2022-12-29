package OverflowGateBot.lib.discord.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.user.DataCache;
import OverflowGateBot.main.BotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import static OverflowGateBot.OverflowGateBot.*;

public class SimpleTable extends DataCache {

    private List<EmbedBuilder> tables = new ArrayList<EmbedBuilder>();
    private List<TableButton> buttons = new ArrayList<TableButton>();
    protected final SlashCommandInteractionEvent event;
    protected int pageNumber = 0;
    protected boolean showPageNumber = true;
    protected Message message;

    public SimpleTable(SlashCommandInteractionEvent event, int aliveLimit) {
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
        if (value == null || value.isEmpty())
            return false;
        return tables.add(new EmbedBuilder(value));
    }

    public SimpleTable addButton(@Nonnull String buttonName, @Nonnull Runnable r) {
        Button button = Button.primary(getId() + ":" + buttonName, buttonName);
        TableButton tableButton = new TableButton(button, r);

        buttons.add(tableButton);
        return this;
    }

    public SimpleTable addButtonSuccess(@Nonnull String buttonName, @Nonnull Emoji emo, @Nonnull Runnable r) {
        Button button = Button.success(getId() + ":" + buttonName, emo);
        TableButton tableButton = new TableButton(button, r);

        buttons.add(tableButton);
        return this;
    }

    public SimpleTable addButtonDeny(@Nonnull String buttonName, @Nonnull Runnable r) {
        Button button = Button.danger(getId() + ":" + buttonName, buttonName);
        TableButton tableButton = new TableButton(button, r);

        buttons.add(tableButton);
        return this;
    }

    public @Nonnull Collection<Button> getButton() {
        Collection<Button> temp = new ArrayList<Button>();
        for (TableButton key : buttons)
            temp.add(key.getButton());
        return temp;
    }

    public void clearButton() {
        this.buttons.clear();
    }

    public MessageEmbed getCurrentPage() {
        EmbedBuilder value = this.tables.get(pageNumber);
        return addPageFooter(value).build();
    }

    public EmbedBuilder addPageFooter(EmbedBuilder value) {
        if (showPageNumber)
            return value.setFooter("Trang " + (pageNumber + 1) + "\\" + getMaxPage());
        return value;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        update();
    }

    public int getMaxPage() {
        return this.tables.size();
    }

    public void nextPage() {
        this.pageNumber += 1;
        this.pageNumber %= getMaxPage();
        this.update();
    }

    public void previousPage() {
        this.pageNumber -= 1;
        if (this.pageNumber <= -1)
            this.pageNumber = getMaxPage() - 1;
        this.update();
    }

    public void firstPage() {
        this.pageNumber = 0;
        this.update();
    }

    public void lastPage() {
        this.pageNumber = getMaxPage() - 1;
        this.update();
    }

    public void delete() {
        this.event.getHook().deleteOriginal().queue();
        this.kill();
    }

    public void send() {
        if (getMaxPage() <= 0) {
            event.getHook().editOriginal("```Không có dữ liệu```").queue();
            return;
        }
        MessageEmbed message = getCurrentPage();
        if (message == null) {
            event.getHook().editOriginal("```Đã hết dữ liệu```").queue();
            return;
        }
        Collection<Button> temp = new ArrayList<Button>();
        for (TableButton key : buttons)
            temp.add(key.getButton());
        event.getHook().sendMessageEmbeds(message).addActionRow(temp).queue();
        tableEmbedMessageHandler.add(this);

    }

    public void update() {
        this.reset();
        if (getMaxPage() <= 0) {
            event.getHook().editOriginal("```Không có dữ liệu```").queue();
            return;
        }
        MessageEmbed message = getCurrentPage();
        if (message == null) {
            event.getHook().editOriginal("```Đã hết dữ liệu```").queue();
            return;
        }

        this.event.getHook().editOriginalEmbeds(message).setActionRow(getButton()).queue();
    }

    public void onCommand(@Nonnull ButtonInteractionEvent event) {
        this.message = event.getMessage();
        String key = event.getComponentId();
        for (TableButton b : buttons) {
            String id = b.getId();
            if (id == null)
                continue;
            if (id.equals(key)) {
                b.getRunnable().run();
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
