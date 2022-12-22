package OverflowGateBot.lib.discord.command.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.RefreshSlashCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.ReloadServerCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.SetChannelCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.SetRoleCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.ShowLevelCommand;

public class AdminCommand extends BotCommandClass {

    public AdminCommand() {
        super("admin", "Admin only");
        addSubcommands(new RefreshSlashCommand());
        addSubcommands(new ReloadServerCommand());
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetRoleCommand());
        addSubcommands(new ShowLevelCommand());
    }

    @Override
    public String getHelpString() {
        return "Lệnh dành riêng cho admin";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (!userHandler.isAdmin(event.getMember()) && !userHandler.isShar(event.getMember())) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        runCommand(event);
    }
}
