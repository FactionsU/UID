package dev.kitteh.factions.listener;

import dev.kitteh.factions.*;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public class FactionsEntityListener extends AbstractListener {

    public final FactionsPlugin plugin;

    public FactionsEntityListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            FactionsPlugin.instance().landRaidControl().onDeath((Player) entity);
        }
    }

    /**
     * Who can I hurt? I can never hurt members or allies. I can always hurt enemies. I can hurt neutrals as long as
     * they are outside their own territory.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
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
            if (!this.canDamagerHurtDamagee(sub, true)) {
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageMonitor(EntityDamageEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        //noinspection UnstableApiUsage
        this.handleExplosion(event.getLocation(), event.getEntity(), event, event.getExplosionResult(), event.blockList());
    }

    // mainly for flaming arrows; don't want allies or people in safe zones to be ignited even after damage event is cancelled
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
            return;
        }

        if (!canDamage(event.getCombuster(), event.getEntity(), false)) {
            event.setCancelled(true);
        }
    }

    private static final Set<PotionEffectType> badPotionEffects = new LinkedHashSet<>(Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.WEAKNESS, PotionEffectType.WITHER));

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
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
            if (!canDamage((Entity) thrower, target, true)) {
                event.setIntensity(target, 0.0);  // affected entity list doesn't accept modification (so no iter.remove()), but this works
            }
        }
    }

    public boolean isPlayerInSafeZone(Entity damagee) {
        if (!(damagee instanceof Player)) {
            return false;
        }
        return Board.board().factionAt(new FLocation(damagee.getLocation())).isSafeZone();
    }

    public boolean canDamagerHurtDamagee(EntityDamageByEntityEvent sub, boolean notify) {
        return canDamage(sub.getDamager(), sub.getEntity(), notify);
    }

    public static boolean canDamage(Entity damager, Entity damagee, boolean notify) {
        FLocation defLoc = new FLocation(damagee.getLocation());
        Faction defLocFaction = Board.board().factionAt(defLoc);

        // for damage caused by projectiles, getDamager() returns the projectile... what we need to know is the source
        if (damager instanceof Projectile projectile) {

            if (!(projectile.getShooter() instanceof Entity)) {
                return true;
            }

            damager = (Entity) projectile.getShooter();
        }

        if (damager instanceof TNTPrimed || damager instanceof Creeper || damager instanceof ExplosiveMinecart) {
            EntityType type = damagee.getType();
            if (type.name().contains("ITEM_FRAME") || type.name().equals("ARMOR_STAND") || type == EntityType.PAINTING) {
                if (explosionDisallowed(damager, new FLocation(damagee.getLocation()))) {
                    return false;
                }
            }
        }

        if (damager instanceof Player player) {
            Material material = null;
            EntityType type = damagee.getType();
            if (type.name().contains("ITEM_FRAME")) {
                material = Material.ITEM_FRAME;
            } else if (type.name().equals("ARMOR_STAND")) {
                material = Material.ARMOR_STAND;
            }
            if (material != null && !canUseBlock(player, material, damagee.getLocation(), false)) {
                return false;
            }
        }

        if (!(damagee instanceof Player damagedPlayer)) {
            if (FactionsPlugin.instance().conf().factions().protection().isSafeZoneBlockAllEntityDamage() && defLocFaction.isSafeZone()) {
                if (damager instanceof Player && notify) {
                    FPlayers.fPlayers().get((Player) damager).msgLegacy(TL.PERM_DENIED_SAFEZONE.format(TL.GENERIC_ATTACK.toString()));
                }
                return false;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isPeacefulBlockAllEntityDamage() && defLocFaction.isPeaceful()) {
                if (damager instanceof Player && notify) {
                    FPlayers.fPlayers().get((Player) damager).msgLegacy(TL.PERM_DENIED_TERRITORY.format(TL.GENERIC_ATTACK.toString(), defLocFaction.tagLegacy(FPlayers.fPlayers().get((Player) damager))));
                }
                return false;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isTerritoryBlockEntityDamageMatchingPerms() && damager instanceof Player && defLocFaction.isNormal()) {
                FPlayer fPlayer = FPlayers.fPlayers().get((Player) damager);
                if (!defLocFaction.hasAccess(fPlayer, PermissibleActions.DESTROY, defLoc)) {
                    if (notify) {
                        fPlayer.msgLegacy(TL.PERM_DENIED_TERRITORY.format(TL.GENERIC_ATTACK.toString(), defLocFaction.tagLegacy(FPlayers.fPlayers().get((Player) damager))));
                    }
                    return false;
                }
            }
            return true;
        }

        FPlayer defender = FPlayers.fPlayers().get(damagedPlayer);

        Location defenderLoc = defender.asPlayer().getLocation();

        if (damager == damagee) {  // ender pearl usage and other self-inflicted damage
            return true;
        }

        if (FactionsPlugin.instance().conf().plugins().worldGuard().isPVPPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().isCustomPVPFlag(damagedPlayer)) {
            return true;
        }

        // Players can not take attack damage in a SafeZone, or possibly peaceful territory
        if (defLocFaction.noPvPInTerritory()) {
            if (damager instanceof Player plr && plr.canSee(damagedPlayer)) {
                if (notify) {
                    FPlayer attacker = FPlayers.fPlayers().get(plr);
                    attacker.msgLegacy(TL.PLAYER_CANTHURT, (defLocFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
                }
                return false;
            }
            return !defLocFaction.noMonstersInTerritory();
        }

        if (!(damager instanceof Player damagerPlayer)) {
            return true;
        }

        FPlayer attacker = FPlayers.fPlayers().get(damagerPlayer);
        notify = notify && damagerPlayer.canSee(damagedPlayer);

        if (attacker.asPlayer() == null) {
            return true;
        }

        MainConfig.Factions facConf = FactionsPlugin.instance().conf().factions();
        if (facConf.protection().getPlayersWhoBypassAllProtection().contains(attacker.name())) {
            return true;
        }

        if (attacker.loginPVPDisabled()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_LOGIN, facConf.pvp().getNoPVPDamageToOthersForXSecondsAfterLogin());
            }
            return false;
        }

        Faction locFaction = Board.board().factionAt(new FLocation(damagerPlayer));

        // so we know from above that the defender isn't in a safezone... what about the attacker, sneaky dog that he might be?
        if (locFaction.noPvPInTerritory()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_CANTHURT, (locFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
            }
            return false;
        }

        if (locFaction.isWarZone() && facConf.protection().isWarZoneFriendlyFire()) {
            return true;
        }

        if (facConf.pvp().getWorldsIgnorePvP().contains(defenderLoc.getWorld().getName())) {
            return true;
        }

        Faction defendFaction = defender.faction();
        Faction attackFaction = attacker.faction();

        if (attackFaction.isWilderness() && facConf.pvp().isDisablePVPForFactionlessPlayers()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_REQUIREFACTION);
            }
            return false;
        } else if (defendFaction.isWilderness()) {
            if (defLocFaction == attackFaction && facConf.pvp().isEnablePVPAgainstFactionlessInAttackersLand()) {
                // Allow PVP vs. Factionless in attacker's faction territory
                return true;
            } else if (facConf.pvp().isDisablePVPForFactionlessPlayers()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_FACTIONLESS);
                }
                return false;
            }
        }

        if (!defLocFaction.isWarZone() || facConf.pvp().isDisablePeacefulPVPInWarzone()) {
            if (defendFaction.isPeaceful()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_PEACEFUL);
                }
                return false;
            } else if (attackFaction.isPeaceful()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_PEACEFUL);
                }
                return false;
            }
        }

        Relation relation = defendFaction.relationTo(attackFaction);

        // You can not hurt neutral factions
        if (facConf.pvp().isDisablePVPBetweenNeutralFactions() && relation.isNeutral()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_NEUTRAL);
            }
            return false;
        }

        // Players without faction may be hurt anywhere
        if (!defender.hasFaction()) {
            return true;
        }

        // You can never hurt faction members or allies
        if (relation.isMember() || relation.isAlly() || relation.isTruce()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_CANTHURT, defender.describeToLegacy(attacker));
            }
            return false;
        }

        boolean ownTerritory = defender.isInOwnTerritory();

        // You can not hurt neutrals in their own territory.
        if (ownTerritory && relation.isNeutral()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_NEUTRALFAIL, defender.describeToLegacy(attacker));
                defender.msgLegacy(TL.PLAYER_PVP_TRIED, attacker.describeToLegacy(defender, true));
            }
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
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
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingBreak(HangingBreakEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
            return;
        }

        if (event.getCause() == RemoveCause.EXPLOSION || (event.getCause() == RemoveCause.ENTITY && event instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent) event).getRemover() instanceof Creeper)) {
            Location loc = event.getEntity().getLocation();
            Faction faction = Board.board().factionAt(new FLocation(loc));
            if (faction.noExplosionsInTerritory()) {
                // faction is peaceful and has explosions set to disabled
                event.setCancelled(true);
                return;
            }

            boolean online = faction.hasMembersOnline();
            MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();

            if ((faction.isWilderness() && !protection.getWorldsNoWildernessProtection().contains(loc.getWorld().getName()) && (protection.isWildernessBlockCreepers() || protection.isWildernessBlockFireballs() || protection.isWildernessBlockTNT())) ||
                    (faction.isNormal() && (online ? (protection.isTerritoryBlockCreepers() || protection.isTerritoryBlockFireballs() || protection.isTerritoryBlockTNT()) : (protection.isTerritoryBlockCreepersWhenOffline() || protection.isTerritoryBlockFireballsWhenOffline() || protection.isTerritoryBlockTNTWhenOffline()))) ||
                    (faction.isWarZone() && (protection.isWarZoneBlockCreepers() || protection.isWarZoneBlockFireballs() || protection.isWarZoneBlockTNT())) ||
                    faction.isSafeZone()) {
                // explosion which needs prevention
                event.setCancelled(true);
                return;
            }
        }

        if (!(event instanceof HangingBreakByEntityEvent)) {
            return;
        }

        Entity breaker = ((HangingBreakByEntityEvent) event).getRemover();
        if (!(breaker instanceof Player)) {
            return;
        }

        if (!FactionsBlockListener.playerCanBuildDestroyBlock((Player) breaker, event.getEntity().getLocation(), PermissibleActions.DESTROY, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPaintingPlace(HangingPlaceEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
            return;
        }

        if (!FactionsBlockListener.playerCanBuildDestroyBlock(event.getPlayer(), event.getBlock().getRelative(event.getBlockFace()).getLocation(), PermissibleActions.BUILD, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!WorldUtil.isEnabled(event.getEntity().getWorld())) {
            return;
        }

        Entity entity = event.getEntity();

        Location loc = event.getBlock().getLocation();

        switch (entity) {
            case Enderman ignored -> {
                if (stopEndermanBlockManipulation(loc)) {
                    event.setCancelled(true);
                }
            }
            case Silverfish ignored -> {
                Faction faction = Board.board().factionAt(new FLocation(loc));
                if (faction.isSafeZone() || faction.isWarZone() || faction.isPeaceful()) {
                    event.setCancelled(true);
                }
            }
            case Wither ignored -> {
                Faction faction = Board.board().factionAt(new FLocation(loc));
                MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
                // it's a bit crude just using fireball protection, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
                if ((faction.isWilderness() && protection.isWildernessBlockFireballs() && !protection.getWorldsNoWildernessProtection().contains(loc.getWorld().getName())) ||
                        (faction.isNormal() && (faction.hasMembersOnline() ? protection.isTerritoryBlockFireballs() : protection.isTerritoryBlockFireballsWhenOffline())) ||
                        (faction.isWarZone() && protection.isWarZoneBlockFireballs()) ||
                        faction.isSafeZone()) {
                    event.setCancelled(true);
                }
            }
            default -> {
            }
        }
    }

    private boolean stopEndermanBlockManipulation(Location loc) {
        if (loc == null) {
            return false;
        }
        // quick check to see if all Enderman deny options are enabled; if so, no need to check location
        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if (protection.isWildernessDenyEndermanBlocks() &&
                protection.isTerritoryDenyEndermanBlocks() &&
                protection.isTerritoryDenyEndermanBlocksWhenOffline() &&
                protection.isSafeZoneDenyEndermanBlocks() &&
                protection.isWarZoneDenyEndermanBlocks()) {
            return true;
        }

        FLocation fLoc = new FLocation(loc);
        Faction claimFaction = Board.board().factionAt(fLoc);

        if (claimFaction.isWilderness()) {
            return protection.isWildernessDenyEndermanBlocks();
        } else if (claimFaction.isNormal()) {
            return claimFaction.hasMembersOnline() ? protection.isTerritoryDenyEndermanBlocks() : protection.isTerritoryDenyEndermanBlocksWhenOffline();
        } else if (claimFaction.isSafeZone()) {
            return protection.isSafeZoneDenyEndermanBlocks();
        } else if (claimFaction.isWarZone()) {
            return protection.isWarZoneDenyEndermanBlocks();
        }

        return false;
    }
}
