package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class UpgradeListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void grow(BlockGrowEvent event) {
        if (!WorldUtil.isEnabled(event.getBlock().getWorld())) {
            return;
        }
        if (!Universe.universe().isUpgradeEnabled(Upgrades.GROWTH)) {
            return;
        }
        FLocation loc = new FLocation(event.getBlock());
        Faction faction = loc.getFaction();
        if (!faction.isNormal()) {
            return;
        }
        int level = faction.getUpgradeLevel(Upgrades.GROWTH);
        if (level == 0) {
            return;
        }

        UpgradeSettings settings = Universe.universe().getUpgradeSettings(Upgrades.GROWTH);

        double chance = settings.valueAt(Upgrades.Variables.CHANCE, level).doubleValue();
        int boost = settings.valueAt(Upgrades.Variables.GROWTH_BOOST, level).intValue();

        if (Math.random() < chance && event.getNewState().getBlockData() instanceof Ageable ageable) {
            ageable.setAge(Math.min(ageable.getAge() + boost, ageable.getMaximumAge()));
            event.getNewState().setBlockData(ageable);
        }
    }
}
