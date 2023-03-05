package OverflowGateBot.main.command.subcommands.SharCommands;

import OverflowGateBot.main.handler.UpdatableHandler;
import OverflowGateBot.main.util.SimpleBotSubcommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UpdateCommand extends SimpleBotSubcommand {
    public UpdateCommand() {
        super("updatecommand", "Shar only", false, true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        UpdatableHandler.updateCommand();
    }
}
