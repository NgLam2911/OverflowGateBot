package OverflowGateBot.main;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.discord.table.SimpleTable;
import arc.util.Log;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static OverflowGateBot.OverflowGateBot.*;

public final class TableHandler extends ListenerAdapter {

    private static TableHandler instance = new TableHandler();
    private static ConcurrentHashMap<String, SimpleTable> tableCache = new ConcurrentHashMap<String, SimpleTable>();

    private TableHandler() {
        jda.addEventListener(this);
        Log.info("Table handler up");
    }

    public static TableHandler getInstance() { return instance; }

    public static void add(SimpleTable table) { tableCache.put(table.getId(), table); }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String component = event.getComponentId();
        String[] id = component.split(":", 2);
        if (id.length <= 1)
            throw new IllegalArgumentException("Invalid component id");

        if (tableCache.containsKey(id[0])) {
            tableCache.get(id[0]).onCommand(event);
        }
    }

    public static void update() {
        Iterator<SimpleTable> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            SimpleTable table = iterator.next();
            if (!table.isAlive(1)) {
                table.delete();
                iterator.remove();
            }
        }
    }
}
