package OverflowGateBot.main.command.subcommands.MindustryCommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import OverflowGateBot.main.handler.ServerStatusHandler;
import OverflowGateBot.main.util.SimpleBotSubcommand;

public class RefreshServerCommand extends SimpleBotSubcommand {
    public RefreshServerCommand() { super("refreshserver", "Làm mới các máy chủ mindustry"); }

    @Override
    public String getHelpString() { return "Làm mới các máy chủ mindustry"; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        ServerStatusHandler.refreshServerStat(event.getGuild(), event.getMessageChannel());
        reply(event, "Đã làm mới máy chủ", 10);
    }

}
