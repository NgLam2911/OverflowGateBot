package OverflowGateBot.main;

import OverflowGateBot.BotConfig;
import arc.util.Log;
import net.dv8tion.jda.api.entities.Activity;

import static OverflowGateBot.OverflowGateBot.*;

import java.util.Timer;
import java.util.TimerTask;

public final class UpdatableHandler {

    private static UpdatableHandler instance = new UpdatableHandler();

    private UpdatableHandler() {
        run("UPDATE", 0, BotConfig.UPDATE_PERIOD, () -> update());

        Log.info("Updatable handler up");
    }

    @Override
    protected void finalize() {
        Log.info("Updatable handler down");
    }

    public static UpdatableHandler getInstance() { return instance; }

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
