package OverflowGateBot.main;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import OverflowGateBot.lib.discord.table.TableEmbedMessageClass;
import arc.util.Log;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static OverflowGateBot.OverflowGateBot.*;

public class TableEmbedMessageHandler extends ListenerAdapter {

    public ConcurrentHashMap<String, TableEmbedMessageClass> tableCache = new ConcurrentHashMap<String, TableEmbedMessageClass>();

    public TableEmbedMessageHandler() {
        jda.addEventListener(this);
        Log.info("Table embed message handler up");
    }

    public void add(TableEmbedMessageClass table) {
        tableCache.put(table.getId(), table);
    }

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

    public void update() {
        Iterator<TableEmbedMessageClass> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            TableEmbedMessageClass table = iterator.next();
            if (!table.isAlive(1)) {
                table.delete();
                iterator.remove();
            }
        }
    }
}
