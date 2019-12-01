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
        return replaceTags(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getTitle());
    }

    @Override
    public List<String> getLines(FPlayer fplayer) {
        if (fplayer.hasFaction()) {
            return getOutput(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getContent());
        } else if (FactionsPlugin.getInstance().conf().scoreboard().constant().isFactionlessEnabled()) {
            return getOutput(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getFactionlessContent());
        }
        return getOutput(fplayer, FactionsPlugin.getInstance().conf().scoreboard().constant().getContent()); // no faction, factionless-board disabled
    }

    public List<String> getOutput(FPlayer fplayer, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }

        lines = new ArrayList<>(lines);

        ListIterator<String> it = lines.listIterator();
        while (it.hasNext()) {
            it.set(replaceTags(fplayer, it.next()));
        }
        return lines;
    }
}
