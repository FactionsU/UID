package dev.kitteh.factions.listener;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WorldUtil;
import org.bukkit.Chunk;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.Wither;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractListener implements Listener {
    public boolean playerCanInteractHere(Player player, Location location) {
        return canInteractHere(player, location);
    }

    public static boolean canInteractHere(Player player, Location location) {
        String name = player.getName();
        if (FactionsPlugin.instance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.board().factionAt(loc);

        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        MainConfig.Factions.Protection protection = FactionsPlugin.instance().conf().factions().protection();
        if (otherFaction.isWilderness()) {
            if (!protection.isWildernessDenyUsage() || protection.getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }
            me.msgLegacy(TL.PLAYER_USE_WILDERNESS, "this");
            return false;
        } else if (otherFaction.isSafeZone()) {
            if (!protection.isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return true;
            }
            me.msgLegacy(TL.PLAYER_USE_SAFEZONE, "this");
            return false;
        } else if (otherFaction.isWarZone()) {
            if (!protection.isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return true;
            }
            me.msgLegacy(TL.PLAYER_USE_WARZONE, "this");

            return false;
        }

        boolean access = otherFaction.hasAccess(me, PermissibleActions.ITEM, loc);

        // Cancel if we are not in our own territory
        if (!access) {
            me.msgLegacy(TL.PLAYER_USE_TERRITORY, "this", otherFaction.tagLegacy(me.faction()));
            return false;
        }

        return true;
    }

    protected void handleExplosion(Location loc, Entity boomer, Cancellable event, @SuppressWarnings("UnstableApiUsage") ExplosionResult result, List<Block> blockList) {
        if (!WorldUtil.isEnabled(loc)) {
            return;
        }

        if (explosionDisallowed(boomer, new FLocation(loc))) {
            event.setCancelled(true);
            return;
        }

        //noinspection UnstableApiUsage
        if (result == ExplosionResult.TRIGGER_BLOCK && boomer != null &&
                FactionsPlugin.instance().conf().factions().protection().isTerritoryBlockWindChargeInteractionMatchingPerms() &&
                boomer instanceof WindCharge charge && charge.getShooter() instanceof Player shooter) {
            blockList.removeIf(block -> !canUseBlock(shooter, block.getType(), block.getLocation(), true));
        }

        List<Chunk> chunks = blockList.stream().map(Block::getChunk).distinct().collect(Collectors.toList());
        if (chunks.removeIf(chunk -> explosionDisallowed(boomer, new FLocation(chunk)))) {
            blockList.removeIf(block -> !chunks.contains(block.getChunk()));
        }

        if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && FactionsPlugin.instance().conf().exploits().isTntWaterlog()) {
            // TNT in water/lava doesn't normally destroy any surrounding blocks, which is usually desired behavior, but...
            // this change below provides workaround for waterwalling providing perfect protection,
            // and makes cheap (non-obsidian) TNT cannons require minor maintenance between shots
            Block center = loc.getBlock();
            if (center.isLiquid()) {
                // a single surrounding block in all 6 directions is broken if the material is weak enough
                List<Block> targets = new ArrayList<>();
                targets.add(center.getRelative(0, 0, 1));
                targets.add(center.getRelative(0, 0, -1));
                targets.add(center.getRelative(0, 1, 0));
                targets.add(center.getRelative(0, -1, 0));
                targets.add(center.getRelative(1, 0, 0));
                targets.add(center.getRelative(-1, 0, 0));
                for (Block target : targets) {
                    Material type = target.getType();
                    if (type.isBlock() && type.getBlastResistance() >= 100F) {
                        continue;
                    }
                    if (!explosionDisallowed(boomer, new FLocation(target.getLocation()))) {
                        target.breakNaturally();
                    }
                }
            }
        }
    }

    public static boolean explosionDisallowed(Entity boomer, FLocation location) {
        Faction faction = Board.board().factionAt(location);
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

    public boolean canPlayerUseBlock(Player player, Material material, Location location, boolean justCheck) {
        return canUseBlock(player, material, location, justCheck);
    }

    public static boolean canUseBlock(Player player, Material material, Location location, boolean justCheck) {
        if (FactionsPlugin.instance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return true;
        }

        FPlayer me = FPlayers.fPlayers().get(player);
        if (me.adminBypass()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.board().factionAt(loc);
        String materialName = material.name();

        // no door/chest/whatever protection in wilderness, war zones, or safe zones
        if (!otherFaction.isNormal()) {
            if (material == Material.ITEM_FRAME ||
                    material == Material.GLOW_ITEM_FRAME ||
                    material == Material.ARMOR_STAND) {
                return canInteractHere(player, location);
            }
            return true;
        }

        if (FactionsPlugin.instance().landRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        PermissibleAction action = null;

        if (material == Material.LEVER) {
            action = PermissibleActions.LEVER;
        } else if (materialName.contains("BUTTON")) {
            action = PermissibleActions.BUTTON;
        } else if (materialName.contains("DOOR") || materialName.contains("GATE")) {
            action = PermissibleActions.DOOR;
        } else if (materialName.endsWith("_PLATE")) {
            action = PermissibleActions.PLATE;
        } else if (materialName.contains("SIGN")) {
            action = PermissibleActions.ITEM;
        } else if (material == Material.CHEST ||
                material == Material.ENDER_CHEST ||
                material == Material.TRAPPED_CHEST ||
                material == Material.BARREL ||
                material == Material.DROPPER ||
                material == Material.DISPENSER ||
                material == Material.HOPPER ||
                materialName.contains("CAULDRON") ||
                material == Material.CAMPFIRE ||
                material == Material.BREWING_STAND ||
                material == Material.CARTOGRAPHY_TABLE ||
                material == Material.GRINDSTONE ||
                material == Material.SMOKER ||
                material == Material.STONECUTTER ||
                material == Material.LECTERN ||
                material == Material.ITEM_FRAME ||
                material == Material.GLOW_ITEM_FRAME ||
                material == Material.JUKEBOX ||
                material == Material.ARMOR_STAND ||
                material == Material.REPEATER ||
                material == Material.ENCHANTING_TABLE ||
                material == Material.BEACON ||
                material == Material.CHIPPED_ANVIL ||
                material == Material.DAMAGED_ANVIL ||
                material == Material.FLOWER_POT ||
                materialName.contains("POTTED") ||
                material == Material.BEE_NEST ||
                materialName.contains("SHULKER") ||
                materialName.contains("ANVIL") ||
                materialName.startsWith("POTTED") ||
                materialName.contains("FURNACE") ||
                FactionsPlugin.instance().conf().factions().protection().getCustomContainers().contains(material)
        ) {
            action = PermissibleActions.CONTAINER;
        }

        if (action == null) {
            return true;
        }

        // Ignored types
        if (action == PermissibleActions.CONTAINER &&
                (
                        FactionsPlugin.instance().conf().factions().protection().getContainerExceptions().contains(material) ||
                                (
                                        otherFaction.isNormal() &&
                                                material == Material.LECTERN &&
                                                FactionsPlugin.instance().conf().factions().protection().isTerritoryAllowLecternReading()
                                )
                )) {
            return true;
        }

        // F PERM check runs through before other checks.
        if (!otherFaction.hasAccess(me, action, loc)) {
            if (action != PermissibleActions.PLATE && !justCheck) {
                me.msgLegacy(TL.GENERIC_NOPERMISSION, action.shortDescription());
            }
            return false;
        }

        return true;
    }
}
