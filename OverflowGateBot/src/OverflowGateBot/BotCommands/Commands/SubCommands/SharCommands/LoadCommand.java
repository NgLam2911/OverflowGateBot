package OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;

import static OverflowGateBot.OverflowGateBot.userHandler;
import static OverflowGateBot.OverflowGateBot.serverStatus;

public class LoadCommand extends BotSubcommandClass {
    public LoadCommand() {
        super("load", "Shar only");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        reply(event, "Đang tải...", 10);
        try {
            serverStatus.load();
            userHandler.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
