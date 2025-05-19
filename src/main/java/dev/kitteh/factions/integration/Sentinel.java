package dev.kitteh.factions.integration;

import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.util.List;
import java.util.logging.Level;

public class Sentinel extends SentinelIntegration {
    public static final String TARGET_FACTIONS = "factions";
    public static final String TARGET_FACTIONS_ENEMY = "factionsEnemy";
    public static final String TARGET_FACTIONS_ALLY = "factionsAlly";
    public static final List<String> TARGETS = List.of(TARGET_FACTIONS, TARGET_FACTIONS_ALLY, TARGET_FACTIONS_ENEMY);

    public static boolean init(Plugin plugin) {
        AbstractFactionsPlugin.getInstance().getLogger().info("Attempting to integrate with Sentinel!");
        try {
            ((SentinelPlugin) plugin).registerIntegration(new Sentinel());
        } catch (Exception e) {
            AbstractFactionsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not load Sentinel integration", e);
            return false;
        }
        AbstractFactionsPlugin.getInstance().getLogger().info("Loaded Sentinel integration!");
        AbstractFactionsPlugin.getInstance().getLogger().info("");
        AbstractFactionsPlugin.getInstance().getLogger().info("You may safely ignore the Sentinel message warning you about compatibility, as we run our own integration.");
        AbstractFactionsPlugin.getInstance().getLogger().info("");
        return true;
    }

    /*
     * Everything below this point is adapted from the Sentinel plugin's integration.
     * It has been heavily rewritten, but credit is important! :)
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
        return TARGET_FACTIONS + ":FACTION_NAME, " + TARGET_FACTIONS_ENEMY + ":NAME, " + TARGET_FACTIONS_ALLY + ":NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[]{TARGET_FACTIONS, TARGET_FACTIONS_ENEMY, TARGET_FACTIONS_ALLY};
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        if (!(ent instanceof Player) || !TARGETS.contains(prefix)) {
            return false;
        }
        Faction faction = Factions.factions().get(value);
        if (faction == null) {
            return false;
        }
        Faction plf = FPlayers.fPlayers().get((Player) ent).faction();
        return switch (prefix) {
            case TARGET_FACTIONS -> faction == plf;
            case TARGET_FACTIONS_ENEMY -> faction.relationTo(plf).equals(Relation.ENEMY);
            case TARGET_FACTIONS_ALLY -> faction.relationTo(plf).equals(Relation.ALLY);
            default -> false;
        };
    }
}
