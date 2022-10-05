package OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands;


import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.messagesHandler;

public class InfoCommand extends BotSubcommandClass {
    public InfoCommand() {
        super("info", "Hiển thị thông tin cơ bản của bot");
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        Member shar = guild.getMemberById("719322804549320725");
        if (shar == null)
            field.append("\tChủ nhân: Sharlotte\n");
        else
            field.append("\tChủ nhân: " + shar.getEffectiveName() + "\n");
        field.append("\tMáy chủ đã tham gia: " + messagesHandler.jda.getGuilds().size() + "\n");
        builder.addField("Thông tin: ", "```" + field.toString() + "```", false);
        replyEmbeds(event, builder, 30);
    }
}
