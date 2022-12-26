package OverflowGateBot.lib.discord.table.tables;

import OverflowGateBot.lib.discord.table.SimpleTable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DeletedMessageTable extends SimpleTable {

    public DeletedMessageTable(SlashCommandInteractionEvent event, int aliveLimit) {
        super(event, aliveLimit);
        addButton("<<<", () -> this.firstPage());
        addButton("<", () -> this.previousPage());
        addButton("X", () -> this.delete());
        addButton(">", () -> this.nextPage());
        addButton(">>>", () -> this.lastPage());
    }
}
