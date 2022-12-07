package OverflowGateBot.command.commands;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.subcommands.AdminCommands.RefreshSlashCommand;
import OverflowGateBot.command.commands.subcommands.AdminCommands.ReloadServerCommand;
import OverflowGateBot.command.commands.subcommands.AdminCommands.SetChannelCommand;
import OverflowGateBot.command.commands.subcommands.AdminCommands.SetRoleCommand;

public class AdminCommand extends BotCommandClass {

    public AdminCommand() {
        super("admin", "Admin only");
        addSubcommands(new RefreshSlashCommand());
        addSubcommands(new ReloadServerCommand());
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetRoleCommand());
    }

    @Override
    public String getHelpString() {
        return "Lệnh dành riêng cho admin";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        if (!guildHandler.isAdmin(member)) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        runCommand(event);
    }
}
