package OverflowGateBot.lib.discord.command.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.command.SimpleBotCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.AddCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.SayCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.SetRoleCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.UpdateCommand;

import static OverflowGateBot.OverflowGateBot.userHandler;

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
        if (!userHandler.isShar(event.getMember())) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        runCommand(event);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {

        if (!userHandler.isShar(event.getMember())) {
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
            return;
        }
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }
}
