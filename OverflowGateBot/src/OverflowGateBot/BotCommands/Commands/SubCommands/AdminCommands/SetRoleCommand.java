package OverflowGateBot.BotCommands.Commands.SubCommands.AdminCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetRoleCommand extends BotSubcommandClass {
    public SetRoleCommand() {
        super("setrole", "Cài đặt các vai trò của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại vai trò muốn đặt", true, true).//
                addOption(OptionType.ROLE, "role", "Vai trò muốn gán", true);
    }

    @Override
    public String getHelpString() {
        return "";
    }

    // TODO SetRoleCommand
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
    }

}
