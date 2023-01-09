package OverflowGateBot.main;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.discord.context.SimpleBotContextMenu;
import OverflowGateBot.lib.discord.context.contexts.DeleteMessageContextMenu;
import OverflowGateBot.lib.discord.context.contexts.PostMapContextMenu;
import OverflowGateBot.lib.discord.context.contexts.PostSchematicContextMenu;
import arc.util.Log;

import static OverflowGateBot.OverflowGateBot.*;

public final class ContextMenuHandler extends ListenerAdapter {

    private static ContextMenuHandler instance = new ContextMenuHandler();
    private static HashMap<String, SimpleBotContextMenu> commands;

    private ContextMenuHandler() {

        commands = new HashMap<>();
        addCommand(new PostMapContextMenu());
        addCommand(new PostSchematicContextMenu());
        addCommand(new DeleteMessageContextMenu());

        jda.addEventListener(this);
        Log.info("Context menu handler up");

    }

    @Override
    protected void finalize() {
        Log.info("Context menu handler down");
    }

    public static ContextMenuHandler getInstance() { return instance; }

    public static Collection<SimpleBotContextMenu> getCommands() { return commands.values(); }

    public static void addCommand(SimpleBotContextMenu command) { commands.put(command.name, command); }

    public static void registerCommand(Guild guild) {
        for (SimpleBotContextMenu command : commands.values()) {
            guild.updateCommands().addCommands(command.command).complete();
        }
    }

    public static void unregisterCommand(Guild guild) {
        guild.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                command.delete().complete();
            }
        });
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        event.deferReply().queue();
        handleCommand(event);
    }

    public static void handleCommand(MessageContextInteractionEvent event) {
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

        /*
         * // Shar permission to use bot if
         * (!guildHandler.guildConfigs.containsKey(guild.getId()) &&
         * !member.getId().equals(SHAR_ID)) { reply(event,
         * "Máy chủ của bạn chưa được duyệt, liên hệ admin Shar để được duyệt", 30);
         * return; }
         */

        if (commands.containsKey(command)) {
            commands.get(command).onCommand(event);
            Log.info(MessageHandler.getMessageSender(event.getTarget()) + ": used " + event.getName());
        }
    }

    public static void replyEmbed(MessageContextInteractionEvent event, EmbedBuilder builder, int deleteAfter) { event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS)); }

    public static void reply(MessageContextInteractionEvent event, String content, int deleteAfter) { event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS)); }
}
