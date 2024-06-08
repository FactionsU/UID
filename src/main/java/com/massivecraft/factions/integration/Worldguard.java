package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Worldguard {
    public static final String FLAG_CLAIM_NAME = "fuuid-claim";
    public static final String FLAG_PVP_NAME = "fuuid-pvp";
    public static final String FLAG_NOLOSS_NAME = "fuuid-noloss";
    private static StateFlag FLAG_CLAIM;
    private static StateFlag FLAG_PVP;
    private static StateFlag FLAG_NOLOSS;

    public static void onLoad() {
        FLAG_CLAIM = registerOrGet(FLAG_CLAIM_NAME);
        status(FLAG_CLAIM != null, FLAG_CLAIM_NAME);

        FLAG_PVP = registerOrGet(FLAG_PVP_NAME);
        status(FLAG_PVP != null, FLAG_PVP_NAME);

        FLAG_NOLOSS = registerOrGet(FLAG_NOLOSS_NAME);
        status(FLAG_NOLOSS != null, FLAG_NOLOSS_NAME);
    }

    private static StateFlag registerOrGet(String flagName) {
        try {
            StateFlag flag = new StateFlag(flagName, false);
            WorldGuard.getInstance().getFlagRegistry().register(flag);
            return flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = WorldGuard.getInstance().getFlagRegistry().get(flagName);
            if (existing instanceof StateFlag flag) {
                return flag;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void status(boolean success, String name) {
        FactionsPlugin.getInstance().getLogger().info((success ? "Registered" : "Failed to register") + " flag '" + name + "' with WorldGuard.");
    }

    public boolean isNoLossFlag(Player player) {
        return this.isFlag(player, FLAG_NOLOSS, "noloss");
    }

    public boolean isCustomPVPFlag(Player player) {
        return this.isFlag(player, FLAG_PVP, "PVP");
    }

    private boolean isFlag(Player player, StateFlag flag, String name) {
        if (flag == null) {
            return false;
        }
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        boolean q = query.testState(localPlayer.getLocation(), localPlayer, flag);
        FactionsPlugin.getInstance().debug("Testing " + name + " flag for player " + player.getName() + ": " + q);
        return q;
    }

    // Check if player can build at location by worldguards rules.
    // Returns:
    //	True: Player can build in the region.
    //	False: Player can not build in the region.
    public boolean playerCanBuild(Player player, Location loc) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.testBuild(BukkitAdapter.adapt(loc), localPlayer);
    }

    public boolean checkForRegionsInChunk(Chunk chunk) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(chunk.getWorld()));
        if (regions == null) {
            return false;
        }

        World world = chunk.getWorld();
        int minChunkX = chunk.getX() << 4;
        int minChunkZ = chunk.getZ() << 4;
        int maxChunkX = minChunkX + 15;
        int maxChunkZ = minChunkZ + 15;

        int worldHeight = world.getMaxHeight(); // Allow for heights other than default

        BlockVector3 min = BlockVector3.at(minChunkX, 0, minChunkZ);
        BlockVector3 max = BlockVector3.at(maxChunkX, worldHeight, maxChunkZ);
        ProtectedRegion region = new ProtectedCuboidRegion("wgregionflagcheckforfactions", min, max);
        ApplicableRegionSet set = regions.getApplicableRegions(region);

        if (FactionsPlugin.getInstance().conf().worldGuard().isChecking()) {
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

    public String getVersion() {
        return WorldGuardPlugin.inst().getDescription().getVersion();
    }
}