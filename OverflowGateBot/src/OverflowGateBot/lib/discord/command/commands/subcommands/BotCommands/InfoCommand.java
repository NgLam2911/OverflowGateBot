package OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

import static OverflowGateBot.OverflowGateBot.*;

public class InfoCommand extends BotSubcommandClass {
    public InfoCommand() {
        super("info", "Hiển thị thông tin cơ bản của bot");
    }

    @Override
    public String getHelpString() {
        return "Hiển thị thông tin cơ bản của bot";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        Member shar = guild.getMemberById(SHAR_ID);

        Long totalMember = 0l;

        for (Guild g : jda.getGuilds())
            totalMember += g.getMemberCount();

        if (shar == null)
            field.append("\tChủ nhân: Sharlotte\n");
        else
            field.append("\tChủ nhân: " + shar.getEffectiveName() + "\n");
        field.append("\tMáy chủ: " + guildHandler.guildCache.size() + "\\" + jda.getGuilds().size() + "\n");
        field.append("\tMáy chủ: " + userHandler.userCache.size() + "\\" + totalMember + "\n");
        builder.addField("Thông tin: ", "```" + field.toString() + "```", false);
        replyEmbeds(event, builder, 30);
    }
}
