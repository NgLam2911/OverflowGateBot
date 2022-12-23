package OverflowGateBot.main;

import org.jetbrains.annotations.NotNull;

import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.commands.AdminCommand;
import OverflowGateBot.lib.discord.command.commands.BotCommand;
import OverflowGateBot.lib.discord.command.commands.MindustryCommand;
import OverflowGateBot.lib.discord.command.commands.SharCommand;
import OverflowGateBot.lib.discord.command.commands.UserCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import static OverflowGateBot.OverflowGateBot.*;

public class CommandHandler extends ListenerAdapter {

    public HashMap<String, BotCommandClass> commands = new HashMap<>();

    public List<Guild> guilds;

    public CommandHandler() {

        addCommand(new AdminCommand());
        addCommand(new BotCommand());
        addCommand(new MindustryCommand());
        addCommand(new UserCommand());
        addCommand(new SharCommand());

        jda.addEventListener(this);

        System.out.println("Command handler up");
    }

    public void addCommand(BotCommandClass command) {
        commands.put(command.name, command);
    }

    public void registerCommand(Guild guild) {
        for (BotCommandClass command : commands.values()) {
            guild.upsertCommand(command.command).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        event.deferReply().queue();
        handleCommand(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        String command = event.getName();
        if (commands.containsKey(command))
            commands.get(command).onAutoComplete(event);
    }

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("Guild not exists");

        Member botMember = guild.getMember(jda.getSelfUser());
        if (botMember == null)
            throw new IllegalStateException("Bot not exists");

        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("Member not exists");

        // If bot don't have manager server permission then return
        if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
            reply(event, "Vui lòng cho bot vai trò người quản lí để sử dụng bot", 30);
            return;
        }

        /*
         * // Shar permission to use bot
         * if (!guildHandler.guildConfigs.containsKey(guild.getId()) &&
         * !member.getId().equals(SHAR_ID)) {
         * reply(event,
         * "Máy chủ của bạn chưa được duyệt, liên hệ admin Shar để được duyệt", 30);
         * return;
         * }
         */

        if (commands.containsKey(command)) {
            // Call subcommand
            commands.get(command).onCommand(event);
            // Print to terminal
            System.out.println(messagesHandler.getMessageSender(event) + ": used " + event.getName() + " "
                    + event.getSubcommandName() + " " + event.getOptions().toString());
            // Send to discord log channel
            if (!command.equals("shar")) {
                messagesHandler.log("```" +
                        member.getEffectiveName() + " đã sử dụng " + command + " " + event.getSubcommandName() + "```",
                        guild);
            }
        } else
            reply(event, "Lệnh sai rồi kìa baka", 10);

    }

    void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build())
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```")
                .queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }
}
