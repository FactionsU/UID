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
import org.bukkit.entity.*;
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
        if (FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(name)) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);

        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
            return true;
        }

        MainConfig.Factions.Protection protection = FactionsPlugin.getInstance().conf().factions().protection();
        if (otherFaction.isWilderness()) {
            if (!protection.isWildernessDenyUsage() || protection.getWorldsNoWildernessProtection().contains(location.getWorld().getName())) {
                return true; // This is not faction territory. Use whatever you like here.
            }
            me.msg(TL.PLAYER_USE_WILDERNESS, "this");
            return false;
        } else if (otherFaction.isSafeZone()) {
            if (!protection.isSafeZoneDenyUsage() || Permission.MANAGE_SAFE_ZONE.has(player)) {
                return true;
            }
            me.msg(TL.PLAYER_USE_SAFEZONE, "this");
            return false;
        } else if (otherFaction.isWarZone()) {
            if (!protection.isWarZoneDenyUsage() || Permission.MANAGE_WAR_ZONE.has(player)) {
                return true;
            }
            me.msg(TL.PLAYER_USE_WARZONE, "this");

            return false;
        }

        boolean access = otherFaction.hasAccess(me, PermissibleActions.ITEM, loc);

        // Cancel if we are not in our own territory
        if (!access) {
            me.msg(TL.PLAYER_USE_TERRITORY, "this", otherFaction.getTag(me.getFaction()));
            return false;
        }

        return true;
    }

    protected void handleExplosion(Location loc, Entity boomer, Cancellable event, ExplosionResult result, List<Block> blockList) {
        if (!WorldUtil.isEnabled(loc.getWorld())) {
            return;
        }

        if (explosionDisallowed(boomer, new FLocation(loc))) {
            event.setCancelled(true);
            return;
        }

        if (result == ExplosionResult.TRIGGER_BLOCK && boomer != null &&
                FactionsPlugin.getInstance().conf().factions().protection().isTerritoryBlockWindChargeInteractionMatchingPerms() &&
                boomer instanceof WindCharge charge && charge.getShooter() instanceof Player shooter) {
            blockList.removeIf(block -> !canUseBlock(shooter, block.getType(), block.getLocation(), true));
        }

        List<Chunk> chunks = blockList.stream().map(Block::getChunk).distinct().collect(Collectors.toList());
        if (chunks.removeIf(chunk -> explosionDisallowed(boomer, new FLocation(chunk)))) {
            blockList.removeIf(block -> !chunks.contains(block.getChunk()));
        }

        if ((boomer instanceof TNTPrimed || boomer instanceof ExplosiveMinecart) && FactionsPlugin.getInstance().conf().exploits().isTntWaterlog()) {
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
                    // TODO get resistance value via NMS for future-proofing
                    Material type = target.getType();
                    if (type == Material.AIR ||
                            type == Material.BEDROCK ||
                            type == Material.WATER ||
                            type == Material.LAVA ||
                            type == Material.OBSIDIAN ||
                            type == Material.NETHER_PORTAL ||
                            type == Material.ENCHANTING_TABLE ||
                            type.name().contains("ANVIL") ||
                            type == Material.END_PORTAL ||
                            type == Material.END_PORTAL_FRAME ||
                            type == Material.ENDER_CHEST) {
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
        Faction faction = Board.getInstance().getFactionAt(location);
        boolean online = faction.hasPlayersOnline();
        if (faction.noExplosionsInTerritory() || (faction.isPeaceful() && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisableBoom())) {
            // faction is peaceful and has explosions set to disabled
            return true;
        }
        MainConfig.Factions.Protection protection = FactionsPlugin.getInstance().conf().factions().protection();
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
        if (FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(player.getName())) {
            return true;
        }

        FPlayer me = FPlayers.getInstance().getByPlayer(player);
        if (me.isAdminBypassing()) {
            return true;
        }

        FLocation loc = new FLocation(location);
        Faction otherFaction = Board.getInstance().getFactionAt(loc);
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

        if (FactionsPlugin.getInstance().getLandRaidControl().isRaidable(otherFaction)) {
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
                FactionsPlugin.getInstance().conf().factions().protection().getCustomContainers().contains(material)
        ) {
            action = PermissibleActions.CONTAINER;
        }

        if (action == null) {
            return true;
        }

        // Ignored types
        if (action == PermissibleActions.CONTAINER &&
                (
                        FactionsPlugin.getInstance().conf().factions().protection().getContainerExceptions().contains(material) ||
                                (
                                        otherFaction.isNormal() &&
                                                material == Material.LECTERN &&
                                                FactionsPlugin.getInstance().conf().factions().protection().isTerritoryAllowLecternReading()
                                )
                )) {
            return true;
        }

        // F PERM check runs through before other checks.
        if (!otherFaction.hasAccess(me, action, loc)) {
            if (action != PermissibleActions.PLATE) {
                me.msg(TL.GENERIC_NOPERMISSION, action.getShortDescription());
            }
            return false;
        }

        return true;
    }

    private static boolean isDupeMaterial(Material material) {
        return material.name().contains("SIGN") || material.name().contains("DOOR") || material.name().contains("CHEST");
    }
}
