package com.massivecraft.factions.integration;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Worldguard Region Checking.
 * <p>
 * original author Spathizilla
 */
public class Worldguard6 implements IWorldguard {
    public static final String FLAG_CLAIM_NAME = "fuuid-claim";
    public static final String FLAG_PVP_NAME = "fuuid-pvp";
    public static final String FLAG_NOLOSS_NAME = "fuuid-noloss";
    private static StateFlag FLAG_CLAIM;
    private static StateFlag FLAG_PVP;
    private static StateFlag FLAG_NOLOSS;
    private static Supplier<Boolean> isChecking;

    public static void onLoad(Logger logger, Supplier<Boolean> isChecking) {
        Worldguard6.isChecking = isChecking;
        boolean claimSuccess = false;
        boolean pvpSuccess = false;
        boolean noLossSuccess = false;
        try {
            try {
                StateFlag claimFlag = new StateFlag(FLAG_CLAIM_NAME, true);
                WorldGuardPlugin.inst().getFlagRegistry().register(claimFlag);
                FLAG_CLAIM = claimFlag;
                claimSuccess = true;
            } catch (FlagConflictException e) {
                Flag<?> existing = WorldGuardPlugin.inst().getFlagRegistry().get(FLAG_CLAIM_NAME);
                if (existing instanceof StateFlag) {
                    FLAG_CLAIM = (StateFlag) existing;
                    claimSuccess = true;
                }
            }
            try {
                StateFlag pvpFlag = new StateFlag(FLAG_PVP_NAME, false);
                WorldGuardPlugin.inst().getFlagRegistry().register(pvpFlag);
                FLAG_PVP = pvpFlag;
                pvpSuccess = true;
            } catch (FlagConflictException e) {
                Flag<?> existing = WorldGuardPlugin.inst().getFlagRegistry().get(FLAG_PVP_NAME);
                if (existing instanceof StateFlag) {
                    FLAG_PVP = (StateFlag) existing;
                    pvpSuccess = true;
                }
            }
            try {
                StateFlag noLossFlag = new StateFlag(FLAG_NOLOSS_NAME, false);
                WorldGuardPlugin.inst().getFlagRegistry().register(noLossFlag);
                FLAG_NOLOSS = noLossFlag;
                noLossSuccess = true;
            } catch (FlagConflictException e) {
                Flag<?> existing = WorldGuardPlugin.inst().getFlagRegistry().get(FLAG_NOLOSS_NAME);
                if (existing instanceof StateFlag) {
                    FLAG_NOLOSS = (StateFlag) existing;
                    noLossSuccess = true;
                }
            }
        } catch (Exception ignored) {
            // Nah
        }
        status(logger, claimSuccess, FLAG_CLAIM_NAME);
        status(logger, pvpSuccess, FLAG_PVP_NAME);
        status(logger, noLossSuccess, FLAG_NOLOSS_NAME);
    }

    private static void status(Logger logger, boolean success, String name) {
        logger.info((success ? "Registered" : "Failed to register") + " flag '" + name + "' with WorldGuard.");
    }

    private WorldGuardPlugin wg;

    public Worldguard6(Plugin wg) {
        this.wg = (WorldGuardPlugin) wg;
    }

    public boolean isNoLossFlag(Player player) {
        return this.isFlag(player, FLAG_NOLOSS);
    }

    public boolean isCustomPVPFlag(Player player) {
        return this.isFlag(player, FLAG_PVP);
    }

    private boolean isFlag(Player player, StateFlag flag) {
        if (flag == null) {
            return false;
        }
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return wg.getRegionManager(player.getWorld()).getApplicableRegions(BukkitUtil.toVector(player.getLocation())).testState(localPlayer, flag);
    }

    // Check if player can build at location by worldguards rules.
    // Returns:
    //	True: Player can build in the region.
    //	False: Player can not build in the region.
    public boolean playerCanBuild(Player player, Location loc) {
        World world = loc.getWorld();
        Vector pt = BukkitUtil.toVector(loc);

        return wg.getRegionManager(world).getApplicableRegions(pt).size() > 0 && wg.canBuild(player, loc);
    }

    // Check for Regions in chunk the chunk
    // Returns:
    //   True: Regions found within chunk
    //   False: No regions found within chunk
    public boolean checkForRegionsInChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int minChunkX = chunk.getX() << 4;
        int minChunkZ = chunk.getZ() << 4;
        int maxChunkX = minChunkX + 15;
        int maxChunkZ = minChunkZ + 15;

        int worldHeight = world.getMaxHeight(); // Allow for heights other than default

        BlockVector minChunk = new BlockVector(minChunkX, 0, minChunkZ);
        BlockVector maxChunk = new BlockVector(maxChunkX, worldHeight, maxChunkZ);

        RegionManager regionManager = wg.getRegionManager(world);
        ProtectedRegion region = new ProtectedCuboidRegion("wgregionflagcheckforfactions", minChunk, maxChunk);
        ApplicableRegionSet set = regionManager.getApplicableRegions(region);

        if (isChecking.get()) {
            return set.size() > 0;
        }
        if (FLAG_CLAIM == null) {
            return false;
        }
        for (ProtectedRegion reg : set.getRegions()) {
            StateFlag.State s = reg.getFlag(FLAG_CLAIM);
            if (s == StateFlag.State.DENY) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getVersion() {
        return wg.getDescription().getVersion();
    }
}