package OverflowGateBot.command.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.userHandler;

import OverflowGateBot.command.BotCommandClass;

import static OverflowGateBot.OverflowGateBot.*;

public class RegisterGuildCommand extends BotCommandClass {
    public RegisterGuildCommand() {
        super("registerguild", "Duyệt máy chủ (shar only)");
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

        if (!member.getId().equals(sharId)) {
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }
        // Add guild to registered guilds list
        commandHandler.registerCommand(guild);
        contextMenuHandler.registerCommand(guild);

        boolean result = guildHandler.addGuild(guild.getId());
        if (result) {
            userHandler.loadGuild(guild.getId());
            guildHandler.save();
            reply(event, "Đã duyệt máy chủ", 30);
        } else
            reply(event, "Máy chủ đã được duyệt trước đó", 30);
    }
}
