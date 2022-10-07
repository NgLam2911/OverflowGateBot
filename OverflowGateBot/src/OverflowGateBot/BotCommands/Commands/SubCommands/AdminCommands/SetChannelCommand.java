package OverflowGateBot.BotCommands.Commands.SubCommands.AdminCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetChannelCommand extends BotSubcommandClass {

    public SetChannelCommand() {
        super("setchannel", "Cài đặt các kênh của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại kênh muốn đặt", true, true);
    }


    @Override
    public String getHelpString() {
        return "Cài đặt các kênh của máy chủ:\n\t<type>: loại kênh muốn đặt";
    }

    // TODO SetChannelCommand
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {

    }

}
