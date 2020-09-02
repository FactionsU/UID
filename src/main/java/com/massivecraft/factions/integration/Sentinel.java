package com.massivecraft.factions.integration;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.util.logging.Level;

public class Sentinel extends SentinelIntegration {
    public static void init(Plugin plugin) {
        FactionsPlugin.getInstance().getLogger().info("Disregarding any whining from Sentinel and trying to integrate anyway!");
        try {
            ((SentinelPlugin) plugin).registerIntegration(new Sentinel());
        } catch (Exception e) {
            FactionsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not load Sentinel integration", e);
            return;
        }
        FactionsPlugin.getInstance().getLogger().info("Loaded Sentinel integration!");
    }

    /*
     * Everything below this point is from the Sentinel developer, with only edits to use FactionsUUID classes.
     *
     * The code below was released under this license:
     *
     * Copyright (c) 2019-2020 Alex "mcmonkey" Goodwin
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in all
     * copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     * SOFTWARE.
     */

    @Override
    public String getTargetHelp() {
        return "factions:FACTION_NAME, factionsenemy:NAME, factionsally:NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[]{"factions", "factionsenemy", "factionsally"};
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("factions") && ent instanceof Player) {
                Faction faction = Factions.getInstance().getByTag(value);
                if (faction == null) {
                    return false;
                }
                for (FPlayer pl : faction.getFPlayers()) {
                    if (pl.getPlayer() != null && pl.getPlayer().getUniqueId() != null
                            && pl.getPlayer().getUniqueId().equals(ent.getUniqueId())) {
                        return true;
                    }
                }
            } else if (prefix.equals("factionsenemy") && ent instanceof Player) {
                Faction faction = Factions.getInstance().getByTag(value);
                if (faction == null) {
                    return false;
                }
                Faction plf = FPlayers.getInstance().getByPlayer((Player) ent).getFaction();
                if (faction.getRelationTo(plf).equals(Relation.ENEMY)) {
                    return true;
                }
            } else if (prefix.equals("factionsally") && ent instanceof Player) {
                Faction faction = Factions.getInstance().getByTag(value);
                if (faction == null) {
                    return false;
                }
                Faction plf = FPlayers.getInstance().getByPlayer((Player) ent).getFaction();
                if (faction.getRelationTo(plf).equals(Relation.ALLY)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
