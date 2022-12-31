package OverflowGateBot;

import OverflowGateBot.main.CommandHandler;
import OverflowGateBot.main.ContextMenuHandler;
import OverflowGateBot.main.DatabaseHandler;
import OverflowGateBot.main.GuildHandler;
import OverflowGateBot.main.MessageHandler;
import OverflowGateBot.main.NetworkHandler;
import OverflowGateBot.main.ServerStatusHandler;
import OverflowGateBot.main.TableHandler;
import OverflowGateBot.main.UpdatableHandler;
import OverflowGateBot.main.UserHandler;

import arc.util.Log;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
            UpdatableHandler.getInstance();

            Log.info("Bot online");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
