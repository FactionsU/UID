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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void mobExp(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || !WorldUtil.isEnabled(entity) || entity.getKiller() == null) {
            return;
        }

        Faction territoryFaction = new FLocation(entity.getEyeLocation()).faction();

        int lvl = territoryFaction.upgradeLevel(Upgrades.MOB_EXP);
        if (lvl == 0 || FPlayers.fPlayers().get(entity.getKiller()).faction() != territoryFaction) {
            return;
        }

        double boost = Universe.universe().upgradeSettings(Upgrades.MOB_EXP).valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue();
        boost = Math.max(0, boost);
        event.setDroppedExp((int) Math.round(event.getDroppedExp() * (1 + boost)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void mobDrops(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || !WorldUtil.isEnabled(entity) || entity.getKiller() == null) {
            return;
        }

        Faction territoryFaction = new FLocation(entity.getLocation()).faction();

        int lvl = territoryFaction.upgradeLevel(Upgrades.MOB_DROP);
        if (lvl == 0 || FPlayers.fPlayers().get(entity.getKiller()).faction() != territoryFaction) {
            return;
        }

        UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.MOB_DROP);
        double chance = settings.valueAt(Upgrades.Variables.CHANCE, lvl).doubleValue();
        if (Math.random() >= chance) {
            return;
        }
        int multiplier = settings.valueAt(Upgrades.Variables.POSITIVE_INCREASE, lvl).intValue();
        if (multiplier <= 1) {
            return;
        }
        for (ItemStack drop : event.getDrops()) {
            if (drop != null && !drop.getType().isAir()) {
                drop.setAmount(drop.getAmount() * multiplier);
            }
        }
    }
}
