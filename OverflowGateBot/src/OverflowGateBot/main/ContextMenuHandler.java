package OverflowGateBot.main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import OverflowGateBot.context.BotContextMenuClass;
import OverflowGateBot.context.contexts.PostMapContextMenu;
import OverflowGateBot.context.contexts.PostSchematicContextMenu;

import static OverflowGateBot.OverflowGateBot.*;

public class ContextMenuHandler extends ListenerAdapter {

    public HashMap<String, BotContextMenuClass> commands = new HashMap<>();

    public ContextMenuHandler() {

        jda.addEventListener(this);

        addCommand(new PostMapContextMenu());
        addCommand(new PostSchematicContextMenu());

        for (BotContextMenuClass command : commands.values()) {
            jda.updateCommands().addCommands(command.command).complete();
        }

        for (Guild guild : jda.getGuilds())
            registerCommand(guild);
    }

    public void addCommand(BotContextMenuClass command) {
        commands.put(command.name, command);
    }

    public void registerCommand(Guild guild) {
        for (BotContextMenuClass command : commands.values()) {
            guild.updateCommands().addCommands(command.command).complete();
        }
    }

    public void unregisterCommand(Guild guild) {
        guild.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                command.delete().complete();
            }
        });
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        event.deferReply().queue();
        System.out.println(messagesHandler.getMessageSender(event.getTarget()) + ": used " + event.getName());
        handleCommand(event);

        event.getHook().deleteOriginal().queueAfter(30, TimeUnit.SECONDS);
    }

    public void handleCommand(MessageContextInteractionEvent event) {
        String command = event.getName();

        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Member botMember = guild.getMember(jda.getSelfUser());
        if (botMember == null)
            return;

        Member member = event.getMember();
        if (member == null)
            return;

        // If bot don't have manager server permission then return
        if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
            reply(event, "Vui lòng cho bot vai trò người quản lí để sử dụng bot", 30);
            return;
        }

        // Shar permission to use bot
        if (!guildHandler.guildConfigs.containsKey(guild.getId()) && !member.getId().equals(sharId)) {
            reply(event, "Máy chủ của bạn chưa được duyệt, liên hệ admin Shar để được duyệt", 30);
            return;
        }

        if (commands.containsKey(command))
            commands.get(command).onCommand(event);
    }

    void replyEmbeds(MessageContextInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    void reply(MessageContextInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }
}
