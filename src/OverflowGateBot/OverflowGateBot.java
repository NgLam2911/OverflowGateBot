package OverflowGateBot;

import java.util.Timer;
import java.util.TimerTask;

import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessageHandler;
import OverflowGateBot.main.NetworkHandler;
import OverflowGateBot.main.ServerStatusHandler;
import OverflowGateBot.main.TableHandler;
import OverflowGateBot.main.UserHandler;

import arc.util.Log;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class OverflowGateBot {

    public static JDA jda;

    public static void main(String[] args) {

        try {
            String TOKEN = System.getenv("TOKEN");

            jda = JDABuilder.createDefault(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(CacheFlag.VOICE_STATE).build();
            jda.awaitReady();

            GuildHandler.getInstance();
            UserHandler.getInstance();
            CommandHandler.getInstance();
            DatabaseHandler.getInstance();
            NetworkHandler.getInstance();
            ServerStatusHandler.getInstance();
            MessageHandler.getInstance();
            TableHandler.getInstance();
            ContextMenuHandler.getInstance();

            // updateCommand();
            run("UPDATE", 0, BotConfig.UPDATE_PERIOD, () -> update());

            Log.info("Bot online");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update() {
        UserHandler.update();
        GuildHandler.update();
        TableHandler.update();
        ServerStatusHandler.update();
        jda.getPresence().setActivity(Activity.playing("with " + GuildHandler.getActiveGuildCount() + " servers | " + UserHandler.getActiveUserCount() + " users"));
    }

    public static void updateCommand() {
        jda.updateCommands().complete();
        CommandHandler.getCommands().forEach(c -> jda.upsertCommand(c.command).complete());
        ContextMenuHandler.getCommands().forEach(c -> jda.upsertCommand(c.command).complete());
    }

    public static void run(String name, long delay, long period, Runnable r) {
        new Timer(name, true).schedule(new TimerTask() {
            @Override
            public void run() { r.run(); }
        }, delay, period);
    }

    public static void run(String name, long delay, Runnable r) {
        new Timer(name, true).schedule(new TimerTask() {
            @Override
            public void run() { r.run(); }
        }, delay);
    }
}
