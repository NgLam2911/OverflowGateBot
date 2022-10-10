package OverflowGateBot.BotCommands.Commands;

import OverflowGateBot.BotCommands.Class.BotCommandClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.guildHandler;
import static OverflowGateBot.OverflowGateBot.commandHandler;

public class UnregisterGuildCommand extends BotCommandClass {
    public UnregisterGuildCommand() {
        super("unregisterguild", "Gỡ duyệt máy chủ (Shar only)");
    }

    @Override
    public String getHelpString(String subcommand) {
        return "Lệnh dành riêng cho shar";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        if (!member.getId().equals("719322804549320725")) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        commandHandler.unregisterCommand(event.getGuild());
        boolean result = guildHandler.guildIds.remove(guild.getId());
        if (result) {
            guildHandler.save();
            reply(event, "Đã gỡ duyệt máy chủ", 30);
        } else
            reply(event, "Máy chủ chưa được duyệt trước đó", 30);
    }
}
