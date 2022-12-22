package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.UserHandler.UserCache;

import static OverflowGateBot.OverflowGateBot.userHandler;

public class HideLevelCommand extends BotSubcommandClass {
    public HideLevelCommand() {
        super("hidelevel", "Ẩn/ tắt ẩn cấp độ của người dùng");
        this.addOption(OptionType.BOOLEAN, "hide", "Ẩn", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/ tắt ẩn cấp độ của người dùng:\n\t<hide>: true để ẩn cấp, false để hiện cấp";
    }
    // TODO

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Boolean hideLevel;
        OptionMapping hideOption = event.getOption("hide");
        if (hideOption == null)
            hideLevel = true;
        else
            hideLevel = hideOption.getAsBoolean();
        Member member = event.getMember();
        if (member == null)
            return;
        UserCache user = userHandler.getUserInstance(member);
        user.data.hideLevel = hideLevel;
        if (hideLevel)
            reply(event, "Đã ẩn level", 10);
        else
            reply(event, "Đã tắt ẩn level", 10);
    }
}
