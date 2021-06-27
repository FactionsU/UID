package com.massivecraft.factions.integration;

import com.massivecraft.factions.FactionsPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
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

public class Worldguard7 implements IWorldguard {
    public static final String FLAG_NAME = "fuuid-claim";
    private static StateFlag FLAG;

    public static void onLoad() {
        boolean success = false;
        try {
            try {
                StateFlag claimFlag = new StateFlag(FLAG_NAME, false);
                WorldGuard.getInstance().getFlagRegistry().register(claimFlag);
                FLAG = claimFlag;
                success = true;
            } catch (FlagConflictException e) {
                Flag<?> existing = WorldGuard.getInstance().getFlagRegistry().get(FLAG_NAME);
                if (existing instanceof StateFlag) {
                    FLAG = (StateFlag) existing;
                    success = true;
                }
            }
        } catch (Exception ignored) {
            // Nah
        }
        FactionsPlugin.getInstance().getLogger().info((success ? "Registered" : "Failed to register") + " flag '" + FLAG_NAME + "' with WorldGuard.");
    }

    // PVP Flag check
    // Returns:
    //   True: PVP is allowed
    //   False: PVP is disallowed
    public boolean isPVP(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.testState(localPlayer.getLocation(), localPlayer, Flags.PVP);
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
        if (FLAG == null) {
            return false;
        }
        for (ProtectedRegion reg : set.getRegions()) {
            StateFlag.State s = reg.getFlag(FLAG);
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