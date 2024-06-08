package com.massivecraft.factions.integration;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IWorldguard {

    boolean isCustomPVPFlag(Player player);

    boolean playerCanBuild(Player player, Location loc);

    boolean checkForRegionsInChunk(Chunk chunk);

    boolean isNoLossFlag(Player player);

    String getVersion();

}
