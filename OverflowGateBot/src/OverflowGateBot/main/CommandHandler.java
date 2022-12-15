package OverflowGateBot.main;

import org.jetbrains.annotations.NotNull;

import OverflowGateBot.command.BotCommandClass;
import OverflowGateBot.command.commands.AdminCommand;
import OverflowGateBot.command.commands.BotCommand;
import OverflowGateBot.command.commands.MindustryCommand;
import OverflowGateBot.command.commands.RegisterGuildCommand;
import OverflowGateBot.command.commands.SharCommand;
import OverflowGateBot.command.commands.UnregisterGuildCommand;
import OverflowGateBot.command.commands.UserCommand;

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

        SharCommand sharCommand = new SharCommand();
        RegisterGuildCommand registerGuildCommand = new RegisterGuildCommand();
        UnregisterGuildCommand unregisterGuildCommand = new UnregisterGuildCommand();

        addCommand(new AdminCommand());
        addCommand(new BotCommand());
        addCommand(new MindustryCommand());
        addCommand(new UserCommand());
        addCommand(sharCommand);
        addCommand(registerGuildCommand);
        addCommand(unregisterGuildCommand);

        jda.addEventListener(this);
        jda.upsertCommand(sharCommand.command).queue();
        jda.upsertCommand(registerGuildCommand.command).queue();
        jda.upsertCommand(unregisterGuildCommand.command).queue();

        for (Guild guild : jda.getGuilds())
            registerCommand(guild);
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
        System.out.println(messagesHandler.getMessageSender(event) + ": used " + event.getName() + " "
                + event.getSubcommandName() + " " + event.getOptions().toString());

        event.deferReply().queue();
        handleCommand(event);

        event.getHook().deleteOriginal().queueAfter(30, TimeUnit.SECONDS);

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

        if (commands.containsKey(command)) {
            commands.get(command).onCommand(event);
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
