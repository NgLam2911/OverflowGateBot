package OverflowGateBot.command.commands;

import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.subcommands.SharCommands.AddCommand;
import OverflowGateBot.command.commands.subcommands.SharCommands.LoadCommand;
import OverflowGateBot.command.commands.subcommands.SharCommands.SaveCommand;
import OverflowGateBot.command.commands.subcommands.SharCommands.SayCommand;
import OverflowGateBot.command.commands.subcommands.SharCommands.SetRoleCommand;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.sharId;

public class SharCommand extends BotCommandClass {

    public SharCommand() {
        super("shar", "Shar only");
        addSubcommands(new AddCommand());
        addSubcommands(new SaveCommand());
        addSubcommands(new LoadCommand());
        addSubcommands(new SayCommand());
        addSubcommands(new SetRoleCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            return;

        if (!member.getId().equals(sharId)) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        runCommand(event);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            return;

        if (!member.getId().equals(sharId)) {
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
            return;
        }
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }
}
