package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.data.GuildData.BOOLEAN_STATE;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;

import static OverflowGateBot.OverflowGateBot.userHandler;

public class ShowLevelCommand extends BotSubcommandClass {
    public ShowLevelCommand() {
        super("showlevel", "Ẩn/ tắt ẩn cấp độ của người dùng");
        this.addOption(OptionType.BOOLEAN, "hide", "Ẩn", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/ tắt ẩn cấp độ của người dùng:\n\t<hide>: true để ẩn cấp, false để hiện cấp";
    }
    // TODO

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Boolean showLevel;
        OptionMapping hideOption = event.getOption("hide");
        if (hideOption == null)
            showLevel = true;
        else
            showLevel = hideOption.getAsBoolean();
        Member member = event.getMember();
        if (member == null)
            return;

        UserData user = userHandler.getUserAwait(member);
        user.showLevel = BOOLEAN_STATE.valueOf(String.valueOf(showLevel));
        if (showLevel)
            reply(event, "Đã ẩn level", 10);
        else
            reply(event, "Đã tắt ẩn level", 10);
    }
}
