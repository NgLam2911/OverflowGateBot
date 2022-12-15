package OverflowGateBot.command.commands.subcommands.AdminCommands;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatus;

import OverflowGateBot.command.BotSubcommandClass;

public class ReloadServerCommand extends BotSubcommandClass {
    public ReloadServerCommand() {
        super("reloadserver", "Tải lại tất cả máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Tải lại tất cả máy chủ mindustry";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }
}