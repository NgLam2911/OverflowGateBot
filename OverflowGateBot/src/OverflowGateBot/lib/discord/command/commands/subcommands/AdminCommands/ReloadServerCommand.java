package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatusHandler;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;

public class ReloadServerCommand extends SimpleBotSubcommand {
    public ReloadServerCommand() {
        super("reloadserver", "Tải lại tất cả máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Tải lại tất cả máy chủ mindustry";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        serverStatusHandler.reloadServer(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }
}
