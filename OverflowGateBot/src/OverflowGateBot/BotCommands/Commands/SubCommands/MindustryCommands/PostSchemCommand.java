package OverflowGateBot.BotCommands.Commands.SubCommands.MindustryCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

public class PostSchemCommand extends BotSubcommandClass {
    public PostSchemCommand() {
        super("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh");
        this.addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true);
    }

    @Override
    public String getHelpString() {
        return "Chuyển tập tin bản thiết kế thành hình ảnh";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        messagesHandler.sendSchematicPreview(event);
        reply(event, "Đăng bản thiết kế thành công", 10);
    }

}
