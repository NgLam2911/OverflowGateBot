package OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatus;

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
        reply(event, "Đang làm mới...", 10);
    }

}
