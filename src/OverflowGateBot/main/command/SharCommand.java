package OverflowGateBot.main.command;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import OverflowGateBot.main.command.subcommands.SharCommands.AddCommand;
import OverflowGateBot.main.command.subcommands.SharCommands.SayCommand;
import OverflowGateBot.main.command.subcommands.SharCommands.SetRoleCommand;
import OverflowGateBot.main.command.subcommands.SharCommands.UpdateCommand;
import OverflowGateBot.main.handler.UserHandler;
import OverflowGateBot.main.util.SimpleBotCommand;

public class SharCommand extends SimpleBotCommand {

    public SharCommand() {
        super("shar", "Shar only");
        addSubcommands(new AddCommand());
        addSubcommands(new SayCommand());
        addSubcommands(new SetRoleCommand());
        addSubcommands(new UpdateCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (!UserHandler.isShar(event.getMember())) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        runCommand(event);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

        if (!UserHandler.isShar(event.getMember())) {
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
            return;
        }
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }
}
