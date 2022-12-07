package OverflowGateBot.command.commands.subcommands.SharCommands;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;

import OverflowGateBot.command.BotSubcommandClass;

import static OverflowGateBot.OverflowGateBot.userHandler;
import static OverflowGateBot.OverflowGateBot.serverStatus;
import static OverflowGateBot.OverflowGateBot.guildHandler;

public class SaveCommand extends BotSubcommandClass {
    public SaveCommand() {
        super("save", "Shar only");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        reply(event, "Đã lưu", 2);
        try {
            serverStatus.save();
            userHandler.save();
            guildHandler.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
