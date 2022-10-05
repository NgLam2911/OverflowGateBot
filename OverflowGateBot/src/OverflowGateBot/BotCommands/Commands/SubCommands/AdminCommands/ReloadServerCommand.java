package OverflowGateBot.BotCommands.Commands.SubCommands.AdminCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.serverStatus;

public class ReloadServerCommand extends BotSubcommandClass {
    public ReloadServerCommand() {
        super("reloadserver", "Tải lại tất cả máy chủ");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
        reply(event, "Đang làm mới...", 10);
    }
}
