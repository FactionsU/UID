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
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public final class Protection {
    public static @Nullable PermissibleAction useActionFromBlockType(Material material) {
        return switch (material) {
            case LEVER -> PermissibleActions.LEVER;
            case CHEST, ENDER_CHEST, TRAPPED_CHEST, BARREL, DROPPER, DISPENSER, HOPPER, CAMPFIRE, BREWING_STAND,
                 CARTOGRAPHY_TABLE, GRINDSTONE, SMOKER, STONECUTTER, LECTERN, ITEM_FRAME, GLOW_ITEM_FRAME, JUKEBOX,
                 ARMOR_STAND, REPEATER, ENCHANTING_TABLE, BEACON, CHIPPED_ANVIL,
                 DAMAGED_ANVIL, FLOWER_POT, BEE_NEST, CHISELED_BOOKSHELF -> PermissibleActions.CONTAINER;

            default -> {
                String materialName = material.name();
                if (materialName.endsWith("BUTTON")) {
                    yield PermissibleActions.BUTTON;
                } else if (materialName.endsWith("DOOR") || materialName.endsWith("GATE")) {
                    yield PermissibleActions.DOOR;
                } else if (materialName.endsWith("_PLATE")) {
                    yield PermissibleActions.PLATE;
                } else if (materialName.endsWith("SIGN")) {
                    yield PermissibleActions.ITEM;
                } else if (materialName.endsWith("CAULDRON") ||
                        materialName.endsWith("SHULKER_BOX") ||
                        materialName.endsWith("ANVIL") ||
                        materialName.startsWith("POTTED") ||
                        materialName.endsWith("FURNACE") ||
                        materialName.endsWith("_SHELF") ||
                        materialName.endsWith("COPPER_CHEST") ||
                        FactionsPlugin.instance().conf().factions().protection().getCustomContainers().contains(material)) {
                    yield PermissibleActions.CONTAINER;
                }

                yield null;
            }
        };
    }

    public static boolean denyBuildOrDestroyBlock(Player player, Block block, PermissibleAction permissibleAction, boolean notify) {
        return denyBuildOrDestroyBlock(player, block.getLocation(), permissibleAction, notify);
    }

    public static boolean denyBuildOrDestroyBlock(Player player, Location location, PermissibleAction permissibleAction, boolean notify) {
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

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionWilderness(), Placeholder.unparsed("action", permissibleAction.shortDescription()));
            }

            return true;
        } else if (otherFaction.isSafeZone()) {
            if (conf.plugins().worldGuard().isBuildPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().playerCanBuild(player, location)) {
                return false;
            }

            if (!conf.factions().protection().isSafeZoneDenyBuild() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionSafezone(), Placeholder.unparsed("action", permissibleAction.shortDescription()));
            }

            return true;
        } else if (otherFaction.isWarZone()) {
            if (conf.plugins().worldGuard().isBuildPriority() && AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().playerCanBuild(player, location)) {
                return false;
            }

            if (!conf.factions().protection().isWarZoneDenyBuild() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionWarzone(), Placeholder.unparsed("action", permissibleAction.shortDescription()));
            }

            return true;
        }
        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return false;
        }

        Faction myFaction = me.faction();
        boolean pain = notify && otherFaction.hasAccess(me, PermissibleActions.PAINBUILD, loc);

        // If the faction hasn't: defined access or denied, fallback to config values
        if (!otherFaction.hasAccess(me, permissibleAction, loc)) {
            if (pain && permissibleAction != PermissibleActions.FROSTWALK) {
                player.damage(conf.factions().other().getActionDeniedPainAmount());
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionTerritoryPain(), FactionResolver.of(me, otherFaction), Placeholder.unparsed("action", permissibleAction.shortDescription()));
                return false;
            } else if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionTerritory(), FactionResolver.of(me, otherFaction), Placeholder.unparsed("action", permissibleAction.shortDescription()));
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

    @Deprecated(forRemoval = true, since = "4.5.0")
    public static boolean denyInteract(Player player, Location location) {
        return denyInteract(player, location, true);
    }

    public static boolean denyInteract(Player player, Location location, boolean notify) {
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
            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseWilderness(),
                        Placeholder.parsed("thing", FactionsPlugin.instance().tl().protection().denied().getUseThis()));
            }
            return true;
        } else if (otherFaction.isSafeZone()) {
            if (!protection.isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }
            if (notify)  {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseSafezone(),
                        Placeholder.parsed("thing", FactionsPlugin.instance().tl().protection().denied().getUseThis()));
            }
            return true;
        } else if (otherFaction.isWarZone()) {
            if (!protection.isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }
            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseWarzone(),
                        Placeholder.parsed("thing", FactionsPlugin.instance().tl().protection().denied().getUseThis()));
            }

            return true;
        }

        boolean access = otherFaction.hasAccess(me, PermissibleActions.ITEM, loc);

        // Cancel if we are not in our own territory
        if (!access) {
            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseTerritory(),
                        FactionResolver.of(me, otherFaction),
                        Placeholder.parsed("thing", FactionsPlugin.instance().tl().protection().denied().getUseThis()));
            }
            return true;
        }

        return false;
    }

    public static boolean denyUseBlock(Player player, Material material, Location location, boolean notify) {
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
                return Protection.denyInteract(player, location, true);
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

        if (action != PermissibleActions.PLATE && notify) {
            me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionTerritory(), FactionResolver.of(me, otherFaction), Placeholder.unparsed("action", action.shortDescription()));
        }
        return true;
    }

    public static boolean denyUseItem(Player player, Location location, Material material, boolean checkDenyList, boolean notify) {
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

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseWilderness(),
                        Placeholder.component("thing", Component.translatable(material.getTranslationKey())));
            }

            return true;
        } else if (otherFaction.isSafeZone()) {
            if (!facConf.protection().isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return false;
            }

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseSafezone(),
                        Placeholder.component("thing", Component.translatable(material.getTranslationKey())));
            }

            return true;
        } else if (otherFaction.isWarZone()) {
            if (!facConf.protection().isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return false;
            }

            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseWarzone(),
                        Placeholder.component("thing", Component.translatable(material.getTranslationKey())));
            }

            return true;
        }

        if (!otherFaction.hasAccess(me, PermissibleActions.ITEM, loc)) {
            if (notify) {
                me.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getUseTerritory(),
                        FactionResolver.of(player, otherFaction),
                        Placeholder.component("thing", Component.translatable(material.getTranslationKey())));
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
            if (material != null && Protection.denyUseBlock(player, material, damagee.getLocation(), true)) {
                return true;
            }
        }

        if (!(damagee instanceof Player damagedPlayer)) {
            if (FactionsPlugin.instance().conf().factions().protection().isSafeZoneBlockAllEntityDamage() && defLocFaction.isSafeZone()) {
                if (damager instanceof Player && notify) {
                    FPlayers.fPlayers().get((Player) damager).sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionSafezone(),
                            Placeholder.unparsed("action", FactionsPlugin.instance().tl().protection().permissions().getAttack()));
                }
                return true;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isPeacefulBlockAllEntityDamage() && defLocFaction.isPeaceful()) {
                if (damager instanceof Player && notify) {
                    FPlayer fPlayer = FPlayers.fPlayers().get((Player) damager);
                    fPlayer.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionTerritory(),
                            FactionResolver.of(fPlayer, defLocFaction),
                            Placeholder.unparsed("action", FactionsPlugin.instance().tl().protection().permissions().getAttack()));
                }
                return true;
            }
            if (FactionsPlugin.instance().conf().factions().protection().isTerritoryBlockEntityDamageMatchingPerms() && damager instanceof Player && defLocFaction.isNormal()) {
                FPlayer fPlayer = FPlayers.fPlayers().get((Player) damager);
                if (!defLocFaction.hasAccess(fPlayer, PermissibleActions.DESTROY, defLoc)) {
                    if (notify) {
                        fPlayer.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getActionTerritory(),
                                FactionResolver.of(fPlayer, defLocFaction),
                                Placeholder.unparsed("action", FactionsPlugin.instance().tl().protection().permissions().getAttack()));
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
                    if (defLocFaction.isSafeZone()) {
                        attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpSafezone());
                    } else if(defLocFaction.isPeaceful()) {
                        attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpPeacefulTerritory());
                    } else { // Unexpected, maybe new feature!
                        attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpCantHurt(), FPlayerResolver.of("target", attacker, defender));
                    }
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
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpLogin(), Placeholder.unparsed("seconds", String.valueOf(facConf.pvp().getNoPVPDamageToOthersForXSecondsAfterLogin())));
            }
            return true;
        }

        Faction locFaction = new FLocation(damagerPlayer).faction();

        // so we know from above that the defender isn't in a safezone... what about the attacker, sneaky dog that he might be?
        if (locFaction.noPvPInTerritory()) {
            if (notify) {
                if (locFaction.isSafeZone()) {
                    attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpSafezone());
                } else if(locFaction.isPeaceful()) {
                    attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpPeacefulTerritory());
                } else { // Unexpected, maybe new feature!
                    attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpCantHurt(), FPlayerResolver.of("target", attacker, defender));
                }
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
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpRequireFaction());
            }
            return true;
        } else if (defendFaction.isWilderness()) {
            if (defLocFaction == attackFaction && facConf.pvp().isEnablePVPAgainstFactionlessInAttackersLand()) {
                // Allow PVP vs. Factionless in attacker's faction territory
                return false;
            } else if (facConf.pvp().isDisablePVPForFactionlessPlayers()) {
                if (notify) {
                    attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpFactionless());
                }
                return true;
            }
        }

        if (!defLocFaction.isWarZone() || facConf.pvp().isDisablePeacefulPVPInWarzone()) {
            if (defendFaction.isPeaceful() || attackFaction.isPeaceful()) {
                if (notify) {
                    attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpPeaceful());
                }
                return true;
            }
        }

        Relation relation = defendFaction.relationTo(attackFaction);

        // You can not hurt neutral factions
        if (facConf.pvp().isDisablePVPBetweenNeutralFactions() && relation.isNeutral()) {
            if (notify) {
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpNeutral());
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
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpCantHurt(), FPlayerResolver.of("target", attacker, defender));
            }
            return true;
        }

        boolean ownTerritory = defender.isInOwnTerritory();

        // You can not hurt neutrals in their own territory.
        if (ownTerritory && relation.isNeutral()) {
            if (notify) {
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpNeutralFail(), FPlayerResolver.of("target", attacker, defender));
                attacker.sendRichMessage(FactionsPlugin.instance().tl().protection().denied().getPvpTried(), FPlayerResolver.of("attacker", defender, attacker));
            }
            return true;
        }

        return false;
    }
}
