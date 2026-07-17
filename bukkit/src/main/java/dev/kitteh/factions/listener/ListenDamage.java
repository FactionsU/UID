package dev.kitteh.factions.listener;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.protection.Protection;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.WorldUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
    private final AbstractFactionsPlugin plugin;

    public ListenDamage(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            FactionsPlugin.instance().landRaidControl().onDeath(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (event.getEntity() instanceof Player plr && FPlayers.fPlayers().get(plr).respawnInvulnerable()) {
            event.setCancelled(true);
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
        } else if (Confs.main().factions().protection().isSafeZonePreventAllDamageToPlayers() && isPlayerInSafeZone(event.getEntity())) {
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawnInvulnerableAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }
        if (damager instanceof Player player) {
            FPlayer fPlayer = FPlayers.fPlayers().get(player);
            if (fPlayer.respawnInvulnerable()) {
                fPlayer.sendRichMessage(Confs.tl().commands().home().getInvulnerableNoMore());
                fPlayer.respawnInvulnerability(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTerritoryDamageBoost(EntityDamageByEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }
        if (!(damager instanceof Player player)) {
            return;
        }

        Faction territoryFaction = new FLocation(player).faction();
        Faction playerFaction = FPlayers.fPlayers().get(player).faction();

        int lvl = territoryFaction.upgradeLevel(Upgrades.TERRITORY_DAMAGE_BOOST);
        if (lvl <= 0 || !(territoryFaction == playerFaction || playerFaction.relationTo(territoryFaction) == Relation.ALLY)) {
            return;
        }

        double boost = Universe.universe().upgradeSettings(Upgrades.TERRITORY_DAMAGE_BOOST).valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue();
        boost = Math.max(0, boost);
        event.setDamage(event.getDamage() * (1 + boost));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTerritoryDamageResistance(EntityDamageByEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity())) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            damager = shooter;
        }
        if (!(damager instanceof Player)) {
            return;
        }

        Faction territoryFaction = new FLocation(player).faction();
        Faction playerFaction = FPlayers.fPlayers().get(player).faction();

        int lvl = territoryFaction.upgradeLevel(Upgrades.TERRITORY_DAMAGE_RESISTANCE);
        if (lvl <= 0 || !(territoryFaction == playerFaction || playerFaction.relationTo(territoryFaction) == Relation.ALLY)) {
            return;
        }

        double reduction = Universe.universe().upgradeSettings(Upgrades.TERRITORY_DAMAGE_RESISTANCE).valueAt(Upgrades.Variables.PERCENT, lvl).doubleValue();
        reduction = Math.clamp(reduction, 0, 1);
        event.setDamage(event.getDamage() * (1 - reduction));
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
                if ((damager instanceof Player) || Confs.main().commands().fly().isDisableOnHurtByMobs()) {
                    cancelFFly((Player) damagee);
                }
            }
            if (damager instanceof Player) {
                cancelWarmup((Player) damager);
                if ((playerHurt && Confs.main().commands().fly().isDisableOnHurtingPlayers()) ||
                        (!playerHurt && Confs.main().commands().fly().isDisableOnHurtingMobs())) {
                    cancelFFly((Player) damager);
                }
            }
        }

        // entity took generic damage?
        if (playerHurt) {
            Player player = (Player) damagee;
            cancelWarmup(player);
            if (Confs.main().commands().fly().isDisableOnGenericDamage()) {
                cancelFFly(player);
            }
        }
    }

    private void cancelFFly(Player player) {
        if (player == null) {
            return;
        }

        var flyTl = Confs.tl().commands().fly();
        FPlayer fPlayer = FPlayers.fPlayers().get(player);
        if (fPlayer.flying()) {
            fPlayer.flying(false, false);
            fPlayer.sendRichMessage(flyTl.getDamage());
            if (fPlayer.autoFlying()) {
                fPlayer.autoFlying(false);
                fPlayer.sendRichMessage(flyTl.getAuto(), Placeholder.unparsed("state", "disabled"));
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
            me.sendRichMessage(Confs.tl().commands().generic().getWarmupCancelled());
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
