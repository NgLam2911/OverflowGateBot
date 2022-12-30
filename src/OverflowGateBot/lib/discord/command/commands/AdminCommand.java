package OverflowGateBot.lib.discord.command.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.command.SimpleBotCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.RefreshSlashCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.ReloadServerCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.SetAdminCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.SetChannelCommand;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.SetLevelRoleCommand;
import OverflowGateBot.main.UserHandler;
import OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands.GuildShowLevelCommand;

public class AdminCommand extends SimpleBotCommand {

    public AdminCommand() {
        super("admin", "Admin only");
        addSubcommands(new RefreshSlashCommand());
        addSubcommands(new ReloadServerCommand());
        addSubcommands(new SetChannelCommand());
        addSubcommands(new SetLevelRoleCommand());
        addSubcommands(new GuildShowLevelCommand());
        addSubcommands(new SetAdminCommand());
    }

    @Override
    public String getHelpString() { return "Lệnh dành riêng cho admin"; }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        if (UserHandler.isAdmin(event.getMember()))
            runCommand(event);
        else
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

    }
}
