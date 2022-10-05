package OverflowGateBot.BotCommands.Commands.SubCommands.AdminCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetChannelCommand extends BotSubcommandClass {

    public SetChannelCommand() {
        super("setchannel", "Cài đặt các kênh của máy chủ");
    }


    @Override
    public String getHelpString() {
        return "";
    }

    // TODO SetChannelCommand
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
    }

}
