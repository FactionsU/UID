package dev.kitteh.factions.protection;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Protection {
    public static @Nullable PermissibleAction useActionFromBlockType(Material material) {
        return switch (material) {
            case LEVER -> PermissibleActions.LEVER;
            case CHEST, ENDER_CHEST, TRAPPED_CHEST, BARREL, DROPPER, DISPENSER, HOPPER, CAMPFIRE, BREWING_STAND,
                 CARTOGRAPHY_TABLE, GRINDSTONE, SMOKER, STONECUTTER, LECTERN, ITEM_FRAME, GLOW_ITEM_FRAME, JUKEBOX,
                 ARMOR_STAND, REPEATER, ENCHANTING_TABLE, BEACON, CHIPPED_ANVIL,
                 DAMAGED_ANVIL, FLOWER_POT, BEE_NEST -> PermissibleActions.CONTAINER;

            default -> {
                String materialName = material.name();
                if (materialName.contains("BUTTON")) {
                    yield PermissibleActions.BUTTON;
                } else if (materialName.contains("DOOR") || materialName.contains("GATE")) {
                    yield PermissibleActions.DOOR;
                } else if (materialName.endsWith("_PLATE")) {
                    yield PermissibleActions.PLATE;
                } else if (materialName.contains("SIGN")) {
                    yield PermissibleActions.ITEM;
                } else if (materialName.contains("CAULDRON") ||
                        materialName.contains("POTTED") ||
                        materialName.contains("SHULKER") ||
                        materialName.contains("ANVIL") ||
                        materialName.startsWith("POTTED") ||
                        materialName.contains("FURNACE") ||
                        FactionsPlugin.instance().conf().factions().protection().getCustomContainers().contains(material)) {
                    yield PermissibleActions.CONTAINER;
                }

                yield null;
            }
        };
    }

    public static boolean denyBuildOrDestroyBlock(Player player, Block block, PermissibleAction permissibleAction, boolean justCheck) {
        return denyBuildOrDestroyBlock(player, block.getLocation(), permissibleAction, justCheck);
    }

    public static boolean denyBuildOrDestroyBlock(Player player, Location location, PermissibleAction permissibleAction, boolean justCheck) {
        String name = player.getName();
        MainConfig conf = FactionsPlugin.instance().conf();
        if (conf.factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return false;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return false;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = loc.faction();

        if (otherFaction.isWilderness()) {
            if (conf.plugins().worldGuard().isBuildPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().playerCanBuild(player, location)) {
                return false;
            }

            if (!conf.factions().protection().isWildernessDenyBuild() || conf.factions().protection().getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return false; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msgLegacy(TL.PERM_DENIED_WILDERNESS, permissibleAction.shortDescription());
            }

            return true;
        } else if (otherFaction.isSafeZone()) {
            if (conf.plugins().worldGuard().isBuildPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().playerCanBuild(player, location)) {
                return false;
            }

            if (!conf.factions().protection().isSafeZoneDenyBuild() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }

            if (!justCheck) {
                me.msgLegacy(TL.PERM_DENIED_SAFEZONE, permissibleAction.shortDescription());
            }

            return true;
        } else if (otherFaction.isWarZone()) {
            if (conf.plugins().worldGuard().isBuildPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().playerCanBuild(player, location)) {
                return false;
            }

            if (!conf.factions().protection().isWarZoneDenyBuild() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }

            if (!justCheck) {
                me.msgLegacy(TL.PERM_DENIED_WARZONE, permissibleAction.shortDescription());
            }

            return true;
        }
        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return false;
        }

        Faction myFaction = me.faction();
        boolean pain = !justCheck && otherFaction.hasAccess(me, PermissibleActions.PAINBUILD, loc);

        // If the faction hasn't: defined access or denied, fallback to config values
        if (!otherFaction.hasAccess(me, permissibleAction, loc)) {
            if (pain && permissibleAction != PermissibleActions.FROSTWALK) {
                player.damage(conf.factions().other().getActionDeniedPainAmount());
                me.msgLegacy(TL.PERM_DENIED_PAINTERRITORY, permissibleAction.shortDescription(), otherFaction.tagLegacy(myFaction));
                return false;
            } else if (!justCheck) {
                me.msgLegacy(TL.PERM_DENIED_TERRITORY, permissibleAction.shortDescription(), otherFaction.tagLegacy(myFaction));
            }
            return true;
        }

        return false;
    }

    public static boolean denyExplode(@Nullable Entity boomer, FLocation location) {
        Faction faction = location.faction();
        boolean online = faction.hasMembersOnline();
        if (faction.noExplosionsInTerritory() || (faction.isPeaceful() && FactionsPlugin.instance().conf().factions().specialCase().isPeacefulTerritoryDisableBoom())) {
            // faction is peaceful and has explosions set to disabled
            return true;
        }
        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if (boomer instanceof Creeper && ((faction.isWilderness() && protection.isWildernessBlockCreepers() && !protection.getWorldsNoWildernessProtection().contains(location.worldName())) ||
                (faction.isNormal() && (online ? protection.isTerritoryBlockCreepers() : protection.isTerritoryBlockCreepersWhenOffline())) ||
                (faction.isWarZone() && protection.isWarZoneBlockCreepers()) ||
                faction.isSafeZone())) {
            // creeper which needs prevention
            return true;
        } else if (
                (boomer instanceof Fireball || boomer instanceof Wither) && ((faction.isWilderness() && protection.isWildernessBlockFireballs() && !protection.getWorldsNoWildernessProtection().contains(location.worldName())) ||
                        (faction.isNormal() && (online ? protection.isTerritoryBlockFireballs() : protection.isTerritoryBlockFireballsWhenOffline())) ||
                        (faction.isWarZone() && protection.isWarZoneBlockFireballs()) ||
                        faction.isSafeZone())) {
            // ghast fireball which needs prevention
            // it's a bit crude just using fireball protection for Wither boss too, but I'd rather not add in a whole new set of xxxBlockWitherExplosion or whatever
            return true;
        } else if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && ((faction.isWilderness() && protection.isWildernessBlockTNT() && !protection.getWorldsNoWildernessProtection().contains(location.worldName())) ||
                (faction.isNormal() && (online ? protection.isTerritoryBlockTNT() : protection.isTerritoryBlockTNTWhenOffline())) ||
                (faction.isWarZone() && protection.isWarZoneBlockTNT()) ||
                (faction.isSafeZone() && protection.isSafeZoneBlockTNT()))) {
            // TNT which needs prevention
            return true;
        } else
            return (faction.isWilderness() && protection.isWildernessBlockOtherExplosions() && !protection.getWorldsNoWildernessProtection().contains(location.worldName())) ||
                    (faction.isNormal() && (online ? protection.isTerritoryBlockOtherExplosions() : protection.isTerritoryBlockOtherExplosionsWhenOffline())) ||
                    (faction.isWarZone() && protection.isWarZoneBlockOtherExplosions()) ||
                    (faction.isSafeZone() && protection.isSafeZoneBlockOtherExplosions());
    }

    public static boolean denyInteract(Player player, Location location) {
        String name = player.getName();
        if (FactionsPlugin.instance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return false;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return false;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = loc.faction();

        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return false;
        }

        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if (otherFaction.isWilderness()) {
            if (!protection.isWildernessDenyUsage() || protection.getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return false; // This is not faction territory. Use whatever you like here.
            }
            me.msgLegacy(TL.PLAYER_USE_WILDERNESS, "this");
            return true;
        } else if (otherFaction.isSafeZone()) {
            if (!protection.isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }
            me.msgLegacy(TL.PLAYER_USE_SAFEZONE, "this");
            return true;
        } else if (otherFaction.isWarZone()) {
            if (!protection.isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }
            me.msgLegacy(TL.PLAYER_USE_WARZONE, "this");

            return true;
        }

        boolean access = otherFaction.hasAccess(me, PermissibleActions.ITEM, loc);

        // Cancel if we are not in our own territory
        if (!access) {
            me.msgLegacy(TL.PLAYER_USE_TERRITORY, "this", otherFaction.tagLegacy(me.faction()));
            return true;
        }

        return false;
    }

    public static boolean denyUseBlock(Player player, Material material, Location location, boolean justCheck) {
        if (FactionsPlugin.instance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return false;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return false;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = loc.faction();

        // no door/chest/whatever protection in wilderness, war zones, or safe zones
        if (!otherFaction.isNormal()) {
            if (material == Material.ITEM_FRAME ||
                    material == Material.GLOW_ITEM_FRAME ||
                    material == Material.ARMOR_STAND) {
                return Protection.denyInteract(player, location);
            }
            return false;
        }

        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return false;
        }

        PermissibleAction action = Protection.useActionFromBlockType(material);

        if (action == null) {
            return false;
        }

        if (action == PermissibleActions.CONTAINER) {
            if (FactionsPlugin.instance().conf().factions().protection().getContainerExceptions().contains(material)) {
                return false;
            }
            if (otherFaction.isNormal() &&
                    material == Material.LECTERN &&
                    FactionsPlugin.instance().conf().factions().protection().isTerritoryAllowLecternReading()) {
                return false;
            }
        }

        if (otherFaction.hasAccess(me, action, loc)) {
            return false;
        }

        if (action != PermissibleActions.PLATE && !justCheck) {
            me.msgLegacy(TL.GENERIC_NOPERMISSION, action.shortDescription());
        }
        return true;
    }

    public static boolean denyUseItem(Player player, Location location, Material material, boolean checkDenyList, boolean justCheck) {
        String name = player.getName();
        MainConfig.Factions facConf = FactionsPlugin.instance().conf().factions();
        if (facConf.protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return false;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return false;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = loc.faction();

        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return false;
        }

        if (checkDenyList) {
            if (otherFaction.hasMembersOnline()) {
                if (!facConf.protection().getTerritoryDenyUsageMaterials().contains(material)) {
                    return false; // Item isn't one we're preventing for online factions.
                }
            } else {
                if (!facConf.protection().getTerritoryDenyUsageMaterialsWhenOffline().contains(material)) {
                    return false; // Item isn't one we're preventing for offline factions.
                }
            }
        }

        if (otherFaction.isWilderness()) {
            if (!facConf.protection().isWildernessDenyUsage() || facConf.protection().getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return false; // This is not faction territory. Use whatever you like here.
            }

            if (!justCheck) {
                me.msgLegacy(TL.PLAYER_USE_WILDERNESS, TextUtil.getMaterialName(material));
            }

            return true;
        } else if (otherFaction.isSafeZone()) {
            if (!facConf.protection().isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }

            if (!justCheck) {
                me.msgLegacy(TL.PLAYER_USE_SAFEZONE, TextUtil.getMaterialName(material));
            }

            return true;
        } else if (otherFaction.isWarZone()) {
            if (!facConf.protection().isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }

            if (!justCheck) {
                me.msgLegacy(TL.PLAYER_USE_WARZONE, TextUtil.getMaterialName(material));
            }

            return true;
        }

        if (!otherFaction.hasAccess(me, PermissibleActions.ITEM, loc)) {
            if (!justCheck) {
                me.msgLegacy(TL.PLAYER_USE_TERRITORY, TextUtil.getMaterialName(material), otherFaction.tagLegacy(me.faction()));
            }
            return true;
        }

        return false;
    }

    public static boolean denyDamage(Entity damager, Entity damagee, boolean notify) {
        FLocation defLoc = new FLocation(damagee.getLocation());
        Faction defLocFaction = defLoc.faction();

        // for damage caused by projectiles, getDamager() returns the projectile... what we need to know is the source
        if (damager instanceof Projectile projectile) {

            if (!(projectile.getShooter() instanceof Entity)) {
                return false;
            }

            damager = (Entity) projectile.getShooter();
        }

        if (damager instanceof TNTPrimed || damager instanceof Creeper || damager instanceof ExplosiveMinecart) {
            EntityType type = damagee.getType();
            if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME ||
                    type == EntityType.ARMOR_STAND || type == EntityType.PAINTING) {
                if (Protection.denyExplode(damager, new FLocation(damagee.getLocation()))) {
                    return true;
                }
            }
        }

        if (damager instanceof Player player) {
            Material material = switch (damagee.getType()) {
                case ITEM_FRAME, GLOW_ITEM_FRAME -> Material.ITEM_FRAME;
                case ARMOR_STAND -> Material.ARMOR_STAND;
                default -> null;
            };
            if (material != null && Protection.denyUseBlock(player, material, damagee.getLocation(), false)) {
                return true;
            }
        }

        if (!(damagee instanceof Player damagedPlayer)) {
            if (FactionsPlugin.instance().conf().factions().protection().isSafeZoneBlockAllEntityDamage() && defLocFaction.isSafeZone()) {
                if (damager instanceof Player && notify) {
                    FPlayers.fPlayers().get((Player) damager).msgLegacy(TL.PERM_DENIED_SAFEZONE.format(TL.GENERIC_ATTACK.toString()));
                }
                return true;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isPeacefulBlockAllEntityDamage() && defLocFaction.isPeaceful()) {
                if (damager instanceof Player && notify) {
                    FPlayers.fPlayers().get((Player) damager).msgLegacy(TL.PERM_DENIED_TERRITORY.format(TL.GENERIC_ATTACK.toString(), defLocFaction.tagLegacy(FPlayers.fPlayers().get((Player) damager))));
                }
                return true;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isTerritoryBlockEntityDamageMatchingPerms() && damager instanceof Player && defLocFaction.isNormal()) {
                FPlayer fPlayer = FPlayers.fPlayers().get((Player) damager);
                if (!defLocFaction.hasAccess(fPlayer, PermissibleActions.DESTROY, defLoc)) {
                    if (notify) {
                        fPlayer.msgLegacy(TL.PERM_DENIED_TERRITORY.format(TL.GENERIC_ATTACK.toString(), defLocFaction.tagLegacy(FPlayers.fPlayers().get((Player) damager))));
                    }
                    return true;
                }
            }
            return false;
        }

        FPlayer defender = FPlayers.fPlayers().get(damagedPlayer);

        Location defenderLoc = damagedPlayer.getLocation();

        if (damager == damagee) {  // ender pearl usage and other self-inflicted damage
            return false;
        }

        if (FactionsPlugin.instance().conf().plugins().worldGuard().isPVPPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().isCustomPVPFlag(damagedPlayer)) {
            return false;
        }

        // Players can not take attack damage in a SafeZone, or possibly peaceful territory
        if (defLocFaction.noPvPInTerritory()) {
            if (damager instanceof Player plr && plr.canSee(damagedPlayer)) {
                if (notify) {
                    FPlayer attacker = FPlayers.fPlayers().get(plr);
                    attacker.msgLegacy(TL.PLAYER_CANTHURT, (defLocFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
                }
                return true;
            }
            return defLocFaction.noMonstersInTerritory();
        }

        if (!(damager instanceof Player damagerPlayer)) {
            return false;
        }

        FPlayer attacker = FPlayers.fPlayers().get(damagerPlayer);
        notify = notify && damagerPlayer.canSee(damagedPlayer);

        if (attacker.asPlayer() == null) {
            return false;
        }

        MainConfig.Factions facConf = FactionsPlugin.instance().conf().factions();
        if (facConf.protection().getPlayersWhoBypassAllProtection().contains(attacker.name())) {
            return false;
        }

        if (attacker.loginPVPDisabled()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_LOGIN, facConf.pvp().getNoPVPDamageToOthersForXSecondsAfterLogin());
            }
            return true;
        }

        Faction locFaction = new FLocation(damagerPlayer).faction();

        // so we know from above that the defender isn't in a safezone... what about the attacker, sneaky dog that he might be?
        if (locFaction.noPvPInTerritory()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_CANTHURT, (locFaction.isSafeZone() ? TL.REGION_SAFEZONE.toString() : TL.REGION_PEACEFUL.toString()));
            }
            return true;
        }

        if (locFaction.isWarZone() && facConf.protection().isWarZoneFriendlyFire()) {
            return false;
        }

        if (facConf.pvp().getWorldsIgnorePvP().contains(defenderLoc.getWorld().getName())) {
            return false;
        }

        Faction defendFaction = defender.faction();
        Faction attackFaction = attacker.faction();

        if (attackFaction.isWilderness() && facConf.pvp().isDisablePVPForFactionlessPlayers()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_REQUIREFACTION);
            }
            return true;
        } else if (defendFaction.isWilderness()) {
            if (defLocFaction == attackFaction && facConf.pvp().isEnablePVPAgainstFactionlessInAttackersLand()) {
                // Allow PVP vs. Factionless in attacker's faction territory
                return false;
            } else if (facConf.pvp().isDisablePVPForFactionlessPlayers()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_FACTIONLESS);
                }
                return true;
            }
        }

        if (!defLocFaction.isWarZone() || facConf.pvp().isDisablePeacefulPVPInWarzone()) {
            if (defendFaction.isPeaceful()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_PEACEFUL);
                }
                return true;
            } else if (attackFaction.isPeaceful()) {
                if (notify) {
                    attacker.msgLegacy(TL.PLAYER_PVP_PEACEFUL);
                }
                return true;
            }
        }

        Relation relation = defendFaction.relationTo(attackFaction);

        // You can not hurt neutral factions
        if (facConf.pvp().isDisablePVPBetweenNeutralFactions() && relation.isNeutral()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_NEUTRAL);
            }
            return true;
        }

        // Players without faction may be hurt anywhere
        if (!defender.hasFaction()) {
            return false;
        }

        // You can never hurt faction members or allies
        if (relation.isMember() || relation.isAlly() || relation.isTruce()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_CANTHURT, defender.describeToLegacy(attacker));
            }
            return true;
        }

        boolean ownTerritory = defender.isInOwnTerritory();

        // You can not hurt neutrals in their own territory.
        if (ownTerritory && relation.isNeutral()) {
            if (notify) {
                attacker.msgLegacy(TL.PLAYER_PVP_NEUTRALFAIL, defender.describeToLegacy(attacker));
                defender.msgLegacy(TL.PLAYER_PVP_TRIED, attacker.describeToLegacy(defender, true));
            }
            return true;
        }

        return false;
    }
}
