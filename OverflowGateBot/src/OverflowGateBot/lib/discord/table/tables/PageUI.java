package OverflowGateBot.lib.discord.table.tables;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.discord.table.TableEmbedMessageClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PageUI extends TableEmbedMessageClass {

    public PageUI(@Nonnull SlashCommandInteractionEvent event) {
        super(event, 2);
        addButton("<<<", () -> this.firstPage());
        addButton("<", () -> this.previousPage());
        addButton("X", () -> this.delete());
        addButton(">", () -> this.nextPage());
        addButton(">>>", () -> this.lastPage());
    }

}
