package dev.kitteh.factions.listener;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class ListenSpawn implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Faction faction = Board.board().factionAt(new FLocation(event.getLocation()));
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        EntityType type = event.getEntityType();
        MainConfig.Factions.Spawning spawning = FactionsPlugin.instance().conf().factions().spawning();

        if (faction.isNormal()) {
            if (faction.isPeaceful() && FactionsPlugin.instance().conf().factions().specialCase().isPeacefulTerritoryDisableMonsters()) {
                if (event.getEntity() instanceof Monster) {
                    event.setCancelled(true);
                }
            }
            if (spawning.getPreventInTerritory().contains(reason) && !spawning.getPreventInTerritoryExceptions().contains(type)) {
                event.setCancelled(true);
            }
        } else if (faction.isSafeZone()) {
            if (spawning.getPreventInSafezone().contains(reason) && !spawning.getPreventInSafezoneExceptions().contains(type)) {
                event.setCancelled(true);
            }
        } else if (faction.isWarZone()) {
            if (spawning.getPreventInWarzone().contains(reason) && !spawning.getPreventInWarzoneExceptions().contains(type)) {
                event.setCancelled(true);
            }
        } else if (faction.isWilderness()) {
            if (spawning.getPreventInWilderness().contains(reason) && !spawning.getPreventInWildernessExceptions().contains(type)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        // if there is a target
        Entity target = event.getTarget();
        if (target == null) {
            return;
        }

        if (event.getEntity() instanceof Monster && Board.board().factionAt(new FLocation(target.getLocation())).noMonstersInTerritory()) {
            event.setCancelled(true);
        }
    }
}
