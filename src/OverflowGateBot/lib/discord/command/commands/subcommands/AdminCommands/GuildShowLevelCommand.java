package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;
import OverflowGateBot.lib.user.GuildData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.guildHandler;

public class GuildShowLevelCommand extends SimpleBotSubcommand {

    public GuildShowLevelCommand() {
        super("guildshowlevel", "Ẩn/Hiện cấp độ của toàn bộ thành viên server");
        this.addOption(OptionType.BOOLEAN, "show", "Ẩn/Hiện", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/Hiện cấp độ của toàn bộ thành viên server\n\tTrue: hiện\n\tFalse: ẩn";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping showOption = event.getOption("show");
        if (showOption == null)
            throw new IllegalStateException("Invalid option");

        String show = String.valueOf(showOption.getAsBoolean());

        GuildData guildCache = guildHandler.getGuild(event.getGuild());
        if (guildCache == null)
            throw new IllegalStateException("No guild data found");

        if (guildCache._displayLevel(show))
            reply(event, "Cập nhật thành công", 10);
        else
            reply(event, "Cập nhật không thành công", 10);
    }
}
