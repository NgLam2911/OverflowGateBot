package OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.updateCommand;

public class UpdateCommand extends SimpleBotSubcommand {
    public UpdateCommand() {
        super("updatecommand", "Shar only", false, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        updateCommand();
    }
}
