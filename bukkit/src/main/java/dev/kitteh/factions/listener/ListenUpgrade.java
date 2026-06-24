package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class ListenUpgrade implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void hunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || !WorldUtil.isEnabled(player)) {
            return;
        }

        if (event.getFoodLevel() >= player.getFoodLevel()) {
            return;
        }

        Faction faction = FPlayers.fPlayers().get(player).faction();
        if (faction.upgradeLevel(Upgrades.NO_HUNGER) > 0 && new FLocation(player).faction() == faction) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void grow(BlockGrowEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock())) {
            return;
        }

        int level = new FLocation(event.getBlock()).faction().upgradeLevel(Upgrades.GROWTH);
        if (level == 0) {
            return;
        }

        UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.GROWTH);

        double chance = settings.valueAt(Upgrades.Variables.CHANCE, level).doubleValue();
        int boost = settings.valueAt(Upgrades.Variables.GROWTH_BOOST, level).intValue();

        if (Math.random() < chance && event.getNewState().getBlockData() instanceof Ageable ageable) {
            ageable.setAge(Math.min(ageable.getAge() + boost, ageable.getMaximumAge()));
            event.getNewState().setBlockData(ageable);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void spawnerRate(SpawnerSpawnEvent event) {
        Block block = event.getSpawner().getBlock();
        if (!WorldUtil.isEnabled(block)) {
            return;
        }

        int lvl = new FLocation(block).faction().upgradeLevel(Upgrades.SPAWNER_RATE);
        if (lvl == 0) {
            return;
        }

        double reduction = Universe.universe().upgradeSettings(Upgrades.SPAWNER_RATE).valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue();
        double multiplier = 1 - Math.clamp(reduction, 0, 1);

        // Wait a tick!
        Bukkit.getScheduler().runTask(AbstractFactionsPlugin.instance(), () -> {
            if (block.getState() instanceof CreatureSpawner current) {
                current.setDelay(Math.max(1, (int) (current.getDelay() * multiplier)));
                current.update();
            }
        });
    }
}
