package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import OverflowGateBot.lib.discord.command.SimpleBotSubcommand;
import OverflowGateBot.lib.user.UserData;
import OverflowGateBot.main.UserHandler;

public class ShowLevelCommand extends SimpleBotSubcommand {
    public ShowLevelCommand() {
        super("showlevel", "Ẩn/ tắt ẩn cấp độ của người dùng");
        this.addOption(OptionType.BOOLEAN, "show", "Hiện", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/ tắt ẩn cấp độ của người dùng:\n\t<show>: true để hiện cấp, false để ẩn cấp";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        Boolean showLevel;
        OptionMapping hideOption = event.getOption("show");
        if (hideOption == null)
            showLevel = true;
        else
            showLevel = hideOption.getAsBoolean();
        Member member = event.getMember();
        if (member == null)
            return;

        UserData user = UserHandler.getUserAwait(member);
        user.showLevel = String.valueOf(showLevel);
        user._displayLevelName();
        if (showLevel)
            reply(event, "Đã ẩn level", 10);
        else
            reply(event, "Đã tắt ẩn level", 10);
    }
}
