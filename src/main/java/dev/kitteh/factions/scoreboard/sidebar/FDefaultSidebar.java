package dev.kitteh.factions.scoreboard.sidebar;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.scoreboard.FSidebarProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FDefaultSidebar extends FSidebarProvider {

    @Override
    public String getTitle(FPlayer fplayer) {
        if (FactionsPlugin.instance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
            return replaceTags(fplayer, FactionsPlugin.instance().conf().scoreboard().constant().getFactionlessTitle());
        }
        return replaceTags(fplayer, FactionsPlugin.instance().conf().scoreboard().constant().getTitle());
    }

    @Override
    public List<String> getLines(FPlayer fplayer) {
        if (FactionsPlugin.instance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
            return getOutput(fplayer, FactionsPlugin.instance().conf().scoreboard().constant().getFactionlessContent());
        }
        return getOutput(fplayer, FactionsPlugin.instance().conf().scoreboard().constant().getContent());
    }

    public List<String> getOutput(FPlayer fplayer, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        List<Component> mapMap = null;
        String mapC = "0123456789abcdef";
        lines = new ArrayList<>(lines);

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next == null) {
                it.remove();
                continue;
            }
            if (next.contains("{map}")) {
                if (mapMap == null) {
                    mapMap = ((MemoryBoard) Board.board()).getScoreboardMap(fplayer);
                }
                String rep = mapMap.isEmpty() ? "" : LegacyComponentSerializer.legacySection().serialize(mapMap.removeFirst());
                if (!rep.isEmpty() && !mapC.isEmpty()) {
                    rep = "\u00A7" + mapC.charAt(0) + "\u00A7r" + rep;
                    mapC = mapC.substring(1);
                }
                next = next.replace("{map}", rep);
            }
            String replaced = replaceTags(fplayer, next);
            if (replaced == null) {
                it.remove();
            } else {
                it.set(replaced);
            }
        }
        return lines;
    }
}
