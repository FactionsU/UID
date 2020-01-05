package com.massivecraft.factions.scoreboards.sidebar;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.scoreboards.FSidebarProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FDefaultSidebar extends FSidebarProvider {

    @Override
    public String getTitle(FPlayer fplayer) {
        if (FactionsPlugin.getInstance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
            return replaceTags(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getFactionlessTitle());
        }
        return replaceTags(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getTitle());
    }

    @Override
    public List<String> getLines(FPlayer fplayer) {
        if (FactionsPlugin.getInstance().conf().scoreboard().constant().isFactionlessEnabled() && !fplayer.hasFaction()) {
            return getOutput(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getFactionlessContent());
        }
        return getOutput(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getContent());
    }

    public List<String> getOutput(FPlayer fplayer, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        lines = new ArrayList<>(lines);

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext()) {
            String next = it.next();
            if (next == null) {
                it.remove();
                continue;
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
