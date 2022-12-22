package OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.updateCommand;

public class UpdateCommand extends BotSubcommandClass {
    public UpdateCommand() {
        super("updatecommand", "Shar only");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        updateCommand();
    }
}
