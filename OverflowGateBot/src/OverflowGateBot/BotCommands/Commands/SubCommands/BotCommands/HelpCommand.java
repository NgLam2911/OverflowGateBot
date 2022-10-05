package OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand extends BotSubcommandClass {
    public HelpCommand() {
        super("help", "Hiển thị thông tin các lệnh");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    // TODO Helpppppppppp
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
    }
}
