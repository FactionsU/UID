package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ListenDamage implements Listener {
    private final FactionsPlugin plugin;

    public ListenDamage(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            FactionsPlugin.instance().landRaidControl().onDeath((Player) entity);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (event.getEntity() instanceof Player plr && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Faction faction = FPlayers.fPlayers().get(plr).faction();
            int lvl = faction.upgradeLevel(Upgrades.FALL_DAMAGE_REDUCTION);
            if (new FLocation(plr).faction() == faction && lvl > 0) {
                UpgradeSettings settings = Universe.universe().upgradeSettings(Upgrades.FALL_DAMAGE_REDUCTION);
                double reduction = settings.valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue();
                reduction = Math.min(1, reduction);
                reduction = Math.max(0, reduction);
                event.setDamage(event.getDamage() * (1 - reduction));
            }
        }

        if (event instanceof EntityDamageByEntityEvent sub) {
            if (Protection.denyDamage(sub.getDamager(), sub.getEntity(), true)) {
                event.setCancelled(true);
            }
        } else if (FactionsPlugin.instance().conf().factions().protection().isSafeZonePreventAllDamageToPlayers() && isPlayerInSafeZone(event.getEntity())) {
            // Players can not take any damage in a Safe Zone
            event.setCancelled(true);
        } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player player) {
            FPlayer fPlayer = FPlayers.fPlayers().get(player);
            if (!fPlayer.takeFallDamage()) {
                event.setCancelled(true); // Falling after /f fly
            }
        }
    }

    private boolean isPlayerInSafeZone(Entity damagee) {
        return damagee instanceof Player plr && new FLocation(plr.getLocation()).faction().isSafeZone();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageMonitor(EntityDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Entity damagee = event.getEntity();
        boolean playerHurt = damagee instanceof Player;

        if (event instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

            if (damager instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Entity) {
                    damager = (Entity) projectile.getShooter();
                }
            }

            if (playerHurt) {
                cancelWarmup((Player) damagee);
                if ((damager instanceof Player) || plugin.conf().commands().fly().isDisableOnHurtByMobs()) {
                    cancelFFly((Player) damagee);
                }
            }
            if (damager instanceof Player) {
                cancelWarmup((Player) damager);
                if ((playerHurt && plugin.conf().commands().fly().isDisableOnHurtingPlayers()) ||
                        (!playerHurt && plugin.conf().commands().fly().isDisableOnHurtingMobs())) {
                    cancelFFly((Player) damager);
                }
            }
        }

        // entity took generic damage?
        if (playerHurt) {
            Player player = (Player) damagee;
            cancelWarmup(player);
            if (plugin.conf().commands().fly().isDisableOnGenericDamage()) {
                cancelFFly(player);
            }
        }
    }

    private void cancelFFly(Player player) {
        if (player == null) {
            return;
        }

        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        if (fPlayer.flying()) {
            fPlayer.flying(false, true);
            if (fPlayer.autoFlying()) {
                fPlayer.autoFlying(false);
            }
        }
    }

    public void cancelWarmup(Player player) {
        if (player == null) {
            return;
        }
        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.warmingUp()) {
            me.cancelWarmup();
            me.msgLegacy(TL.WARMUPS_NOTIFY_CANCELLED);
        }
    }


    // mainly for flaming arrows; don't want allies or people in safe zones to be ignited even after damage event is cancelled
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (Protection.denyDamage(event.getCombuster(), event.getEntity(), false)) {
            event.setCancelled(true);
        }
    }

    private static final Set<PotionEffectType> badPotionEffects = new LinkedHashSet<>(Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    ));

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        // see if the potion has a harmful effect
        boolean badjuju = false;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (badPotionEffects.contains(effect.getType())) {
                badjuju = true;
                break;
            }
        }
        if (!badjuju) {
            return;
        }

        ProjectileSource thrower = event.getPotion().getShooter();
        if (!(thrower instanceof Entity)) {
            return;
        }

        if (thrower instanceof Player player) {
            FPlayer fPlayer = FPlayers.fPlayers().get(player);
            if (fPlayer.faction().isPeaceful()) {
                if (event.getPotion().getEffects().stream().allMatch(e -> e.getType().equals(PotionEffectType.WEAKNESS))) {
                    for (LivingEntity target : event.getAffectedEntities()) {
                        if (target.getType() != EntityType.ZOMBIE_VILLAGER) {
                            event.setIntensity(target, 0);
                        }
                    }
                    return;
                }
                event.setCancelled(true);
                return;
            }
        }

        // scan through affected entities to make sure they're all valid targets
        for (LivingEntity target : event.getAffectedEntities()) {
            if (Protection.denyDamage((Entity) thrower, target, true)) {
                event.setIntensity(target, 0.0);  // affected entity list doesn't accept modification (so no iter.remove()), but this works
            }
        }
    }


}
