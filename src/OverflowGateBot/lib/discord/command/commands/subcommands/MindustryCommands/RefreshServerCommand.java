package OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatusHandler;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;

public class RefreshServerCommand extends SimpleBotSubcommand {
    public RefreshServerCommand() {
        super("refreshserver", "Làm mới các máy chủ mindustry");
    }

    @Override
    public String getHelpString() {
        return "Làm mới các máy chủ mindustry";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        serverStatusHandler.refreshServerStat(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }

}
