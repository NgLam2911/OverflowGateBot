package OverflowGateBot.lib.minigame.tictactoe;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.table.SimpleTable;

public class TicTacToe extends SimpleTable {

    public TicTacToe(SlashCommandInteractionEvent event) { super(event, 10); }


    
}
