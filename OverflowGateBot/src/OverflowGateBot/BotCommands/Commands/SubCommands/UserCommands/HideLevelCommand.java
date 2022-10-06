package OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;

public class HideLevelCommand extends BotSubcommandClass {
    public HideLevelCommand() {
        super("hidelevel", "Ẩn/ tắt ẩn cấp độ của người dùng");
        this.addOption(OptionType.BOOLEAN, "hide", "Ẩn", true);
    }

    @Override
    public String getHelpString() {
        return "Ẩn/ tắt ẩn cấp độ của người dùng\n\t<hide>: true để ẩn cấp, false để hiện cấp";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Boolean hidelv;
        OptionMapping hideOption = event.getOption("hide");
        if (hideOption == null)
            hidelv = true;
        else
            hidelv = hideOption.getAsBoolean();
        userHandler.hidelv(event.getMember(), hidelv);
        if (hidelv)
            reply(event, "Đã ẩn level", 10);
        else
            reply(event, "Đã tắt ẩn level", 10);
    }
}
