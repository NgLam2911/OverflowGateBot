package OverflowGateBot.main.command.subcommands.MindustryCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import OverflowGateBot.main.handler.NetworkHandler;
import OverflowGateBot.main.handler.ServerStatusHandler;
import OverflowGateBot.main.util.SimpleBotSubcommand;

public class PingCommand extends SimpleBotSubcommand {
    public PingCommand() {
        super("ping", "Ping máy chủ mindustry thông qua ip");
        this.addOption(OptionType.STRING, "ip", "Ip của máy chủ", true);
    }

    @Override
    public String getHelpString() { return "Ping máy chủ mindustry thông qua ip:\n\t<IP>: là ip của máy chủ muốn ping (chỉ chấp nhận ipv4) "; }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping ipOption = event.getOption("ip");
        if (ipOption == null)
            return;
        String ip = ipOption.getAsString();
        NetworkHandler.pingServer(ip, result -> {
            EmbedBuilder builder = ServerStatusHandler.serverStatusBuilder(ip, result);
            replyEmbed(event, builder, 30);
        });
    }

}
