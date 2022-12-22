package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.GuildHandler.GuildCache;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.guildHandler;

public class ShowLevelCommand extends BotSubcommandClass {

    public ShowLevelCommand() {
        super("showlevel", "Ẩn/Hiện cấp độ của toàn bộ thành viên server");
        this.addOption(OptionType.BOOLEAN, "show", "Ẩn/Hiện", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/Hiện cấp độ của toàn bộ thành viên server\n\tTrue: hiện\n\tFalse: ẩn";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping showOption = event.getOption("show");
        if (showOption == null)
            return;

        boolean show = showOption.getAsBoolean();
        GuildCache guildCache = guildHandler.getGuild(event.getGuild());
        if (guildCache == null)
            return;
        guildCache.data.setShowLevel(show);
        if (guildCache.data.showLevel == show)
            reply(event, "Cập nhật thành công", 10);
        else
            reply(event, "Cập nhật không thành công", 10);
    }
}
