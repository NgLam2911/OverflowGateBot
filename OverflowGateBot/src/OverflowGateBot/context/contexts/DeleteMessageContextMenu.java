package OverflowGateBot.context.contexts;

import OverflowGateBot.context.BotContextMenuClass;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

public class DeleteMessageContextMenu extends BotContextMenuClass {

    public DeleteMessageContextMenu() {
        super("Delete Message");
    }

    @Override
    protected void runCommand(MessageContextInteractionEvent event) {
        Member member = event.getTarget().getMember();

        if (member == null)
            return;

        if (member.getUser() == jda.getSelfUser()) {
            for (MessageEmbed embed : event.getTarget().getEmbeds()) {
                AuthorInfo authorInfo = embed.getAuthor();
                if (authorInfo == null)
                    continue;
                String name = authorInfo.getName();
                if (name == null)
                    continue;
                Member trigger = event.getMember();
                if (trigger == null)
                    continue;
                if (name.equals(trigger.getEffectiveName())) {
                    event.getTarget().delete().queue();
                    break;
                }
            }
        }
    }
}
