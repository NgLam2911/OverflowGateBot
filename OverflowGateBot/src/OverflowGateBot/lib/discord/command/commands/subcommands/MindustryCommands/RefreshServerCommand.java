package OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatus;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class RefreshServerCommand extends BotSubcommandClass {
    public RefreshServerCommand() {
        super("refreshserver", "Làm mới các máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Làm mới các máy chủ mindustry";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        serverStatus.refreshServerStat(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }

}
