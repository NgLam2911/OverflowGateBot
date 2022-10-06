package OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

public class PostMapCommand extends BotSubcommandClass {
    public PostMapCommand() {
        super("postmap", "Chuyển tập tin bản đồ thành hình ảnh");
        this.addOption(OptionType.ATTACHMENT, "mapfile", "Tập tin map.msv", true);
    }

    @Override
    public String getHelpString() {
        return "Chuyển tập tin bản đồ thành hình ảnh\n\t<mapfile>: Tập tin chứ bản đồ muốn gửi, tập tin phải có định dạng (đuôi) .msav";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        messagesHandler.sendMapPreview(event);
        reply(event, "Đăng bản đồ thành công", 10);
    }

}
