package OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.networkHandler;
import static OverflowGateBot.OverflowGateBot.serverStatusHandler;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class PingCommand extends BotSubcommandClass {
    public PingCommand() {
        super("ping", "Ping máy chủ mindustry thông qua ip");
        this.addOption(OptionType.STRING, "ip", "Ip của máy chủ", true);
    }

    @Override
    public String getHelpString() {
        return "Ping máy chủ mindustry thông qua ip:\n\t<IP>: là ip của máy chủ muốn ping (chỉ chấp nhận ipv4) ";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping ipOption = event.getOption("ip");
        if (ipOption == null)
            return;
        String ip = ipOption.getAsString();
        networkHandler.pingServer(ip, result -> {
            EmbedBuilder builder = serverStatusHandler.serverStatusBuilder(ip, result);
            replyEmbeds(event, builder, 30);
        });
    }

}
