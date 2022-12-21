package OverflowGateBot.lib.discord.command.commands.subcommands.MindustryCommands;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class PostSchemCommand extends BotSubcommandClass {
    public PostSchemCommand() {
        super("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh");
        this.addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true);
    }

    @Override
    public String getHelpString() {
        return "Chuyển tập tin bản thiết kế thành hình ảnh:\n\t<schematicfile>: Tập tin chứ bản thiết kế muốn gửi, tập tin phải có định dạng (đuôi) .msch";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        messagesHandler.sendSchematicPreview(event);
        reply(event, "Đăng bản thiết kế thành công", 10);
    }

}
