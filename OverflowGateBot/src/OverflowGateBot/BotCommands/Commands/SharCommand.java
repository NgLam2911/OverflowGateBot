package OverflowGateBot.BotCommands.Commands;

import OverflowGateBot.BotCommands.Class.BotCommandClass;
import OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands.SetRoleCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands.AddCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands.LoadCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands.SaveCommand;
import OverflowGateBot.BotCommands.Commands.SubCommands.SharCommands.SayCommand;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        if (!member.getId().equals("719322804549320725")) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        runCommand(event);
    }
}
