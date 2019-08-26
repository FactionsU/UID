package com.massivecraft.factions.util;

import java.util.HashSet;

import org.bukkit.World;

import com.massivecraft.factions.FactionsPlugin;

public class WorldUtil {
    protected FactionsPlugin plugin;
    protected HashSet<String> enabledWorlds;
    protected boolean check;

    public WorldUtil(FactionsPlugin plugin) {
        this.plugin = plugin;
        enabledWorlds = new HashSet<String>(plugin.getConfig().getStringList("enabled-worlds.worlds"));
        check = plugin.getConfig().getBoolean("enabled-worlds.check", false);
    }

    public boolean worldCheck() {
        return check;
    }

    public boolean enabledWorld(String worldName) {
        return enabledWorlds.contains(worldName);
    }

    public boolean enabledWorld(World world) {
        return enabledWorld(world.getName());
    }
}
