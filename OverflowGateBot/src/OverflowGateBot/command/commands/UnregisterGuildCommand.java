package OverflowGateBot.command.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.guildHandler;

import OverflowGateBot.command.BotCommandClass;

import static OverflowGateBot.OverflowGateBot.commandHandler;
import static OverflowGateBot.OverflowGateBot.contextMenuHandler;

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

        commandHandler.unregisterCommand(guild);
        contextMenuHandler.unregisterCommand(guild);
        
        boolean result = guildHandler.guildIds.remove(guild.getId());
        if (result) {
            guildHandler.save();
            reply(event, "Đã gỡ duyệt máy chủ", 30);
        } else
            reply(event, "Máy chủ chưa được duyệt trước đó", 30);
    }
}
