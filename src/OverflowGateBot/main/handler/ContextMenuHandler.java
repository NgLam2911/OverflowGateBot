package OverflowGateBot.main.handler;

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

import OverflowGateBot.main.context.DeleteMessageContextMenu;
import OverflowGateBot.main.context.PostMapContextMenu;
import OverflowGateBot.main.context.PostSchemContextMenu;
import OverflowGateBot.main.util.SimpleBotContextMenu;
import arc.util.Log;

import static OverflowGateBot.OverflowGateBot.*;

public final class ContextMenuHandler extends ListenerAdapter {

    private static ContextMenuHandler instance = new ContextMenuHandler();
    private static HashMap<String, SimpleBotContextMenu> commands;

    private ContextMenuHandler() {

        commands = new HashMap<>();
        addCommand(new PostMapContextMenu());
        addCommand(new PostSchemContextMenu());
        addCommand(new DeleteMessageContextMenu());

        jda.addEventListener(this);
        Log.info("Context menu handler up");

    }

    @Override
    protected void finalize() {
        Log.info("Context menu handler down");
    }

    public static ContextMenuHandler getInstance() {
        return instance;
    }

    public static Collection<SimpleBotContextMenu> getCommands() {
        return commands.values();
    }

    public static void addCommand(SimpleBotContextMenu command) {
        commands.put(command.name, command);
    }

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
            reply(event, "Vui l??ng cho bot vai tr?? ng?????i qu???n l?? ????? s??? d???ng bot", 30);
            return;
        }

        /*
         * // Shar permission to use bot if
         * (!guildHandler.guildConfigs.containsKey(guild.getId()) &&
         * !member.getId().equals(SHAR_ID)) { reply(event,
         * "M??y ch??? c???a b???n ch??a ???????c duy???t, li??n h??? admin Shar ????? ???????c duy???t", 30);
         * return; }
         */

        if (commands.containsKey(command)) {
            commands.get(command).onCommand(event);
            Log.info(MessageHandler.getMessageSender(event.getTarget()) + ": used " + event.getName());
        }
    }

    public static void replyEmbed(MessageContextInteractionEvent event, EmbedBuilder builder, int deleteAfter) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void reply(MessageContextInteractionEvent event, String content, int deleteAfter) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }
}
