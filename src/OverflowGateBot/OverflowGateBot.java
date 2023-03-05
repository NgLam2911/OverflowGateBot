package OverflowGateBot;

import OverflowGateBot.main.handler.CommandHandler;
import OverflowGateBot.main.handler.ContextMenuHandler;
import OverflowGateBot.main.handler.DatabaseHandler;
import OverflowGateBot.main.handler.GuildHandler;
import OverflowGateBot.main.handler.MessageHandler;
import OverflowGateBot.main.handler.NetworkHandler;
import OverflowGateBot.main.handler.ServerStatusHandler;
import OverflowGateBot.main.handler.TableHandler;
import OverflowGateBot.main.handler.UpdatableHandler;
import OverflowGateBot.main.handler.UserHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class OverflowGateBot {

    public static JDA jda;

    public static void main(String[] args) {

        try {
            String TOKEN = System.getenv("TOKEN");

            jda = JDABuilder.createDefault(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).build();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
