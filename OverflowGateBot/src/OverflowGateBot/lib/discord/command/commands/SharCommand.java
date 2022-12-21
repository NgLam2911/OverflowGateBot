package OverflowGateBot.lib.discord.command.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.SHAR_ID;

import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.AddCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.SayCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.SharCommands.SetRoleCommand;

public class SharCommand extends BotCommandClass {

    public SharCommand() {
        super("shar", "Shar only");
        addSubcommands(new AddCommand());
        addSubcommands(new SayCommand());
        addSubcommands(new SetRoleCommand());
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null)
            return;

        if (!member.getId().equals(SHAR_ID)) {
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

        if (!member.getId().equals(SHAR_ID)) {
            sendAutoComplete(event, "Bạn không có quyền để sử dụng lệnh này");
            return;
        }
        if (subcommands.containsKey(event.getSubcommandName())) {
            subcommands.get(event.getSubcommandName()).onAutoComplete(event);
        }
    }
}
