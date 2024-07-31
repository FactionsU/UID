package com.massivecraft.factions.util;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.material.MaterialDb;
import com.massivecraft.factions.util.particle.ParticleColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SeeChunkUtil extends BukkitRunnable {

    private final Set<UUID> playersSeeingChunks = new HashSet<>();
    private final boolean useColor;
    private final Particle effect;

    public SeeChunkUtil() {
        String effectName = FactionsPlugin.getInstance().conf().commands().seeChunk().getParticleName();
        this.effect = FactionsPlugin.getInstance().getParticleProvider().effectFromString(effectName);
        this.useColor = FactionsPlugin.getInstance().conf().commands().seeChunk().isRelationalColor();

        FactionsPlugin.getInstance().getLogger().info(FactionsPlugin.getInstance().txt().parse("Using %s as the ParticleEffect for /f sc", FactionsPlugin.getInstance().getParticleProvider().effectName(effect)));
    }

    @Override
    public void run() {
        for (UUID playerId : playersSeeingChunks) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                playersSeeingChunks.remove(playerId);
                continue;
            }
            if (!FactionsPlugin.getInstance().worldUtil().isEnabled(player)) {
                continue;
            }
            FPlayer fme = FPlayers.getInstance().getByPlayer(player);
            showPillars(player, fme, this.effect, useColor);
        }
    }

    public void updatePlayerInfo(UUID uuid, boolean toggle) {
        if (toggle) {
            playersSeeingChunks.add(uuid);
        } else {
            playersSeeingChunks.remove(uuid);
        }
    }

    public static void showPillars(Player me, FPlayer fme, Particle effect, boolean useColor) {
        World world = me.getWorld();
        FLocation flocation = new FLocation(me);
        int chunkX = (int) flocation.getX();
        int chunkZ = (int) flocation.getZ();

        ParticleColor color = null;
        if (useColor) {
            ChatColor chatColor = Board.getInstance().getFactionAt(flocation).getRelationTo(fme).getColor();
            color = ParticleColor.fromChatColor(chatColor);
        }

        int blockX;
        int blockZ;

        blockX = chunkX * 16;
        blockZ = chunkZ * 16;
        showPillar(me, world, blockX, blockZ, effect, color);

        blockX = chunkX * 16 + 16;
        blockZ = chunkZ * 16;
        showPillar(me, world, blockX, blockZ, effect, color);

        blockX = chunkX * 16;
        blockZ = chunkZ * 16 + 16;
        showPillar(me, world, blockX, blockZ, effect, color);

        blockX = chunkX * 16 + 16;
        blockZ = chunkZ * 16 + 16;
        showPillar(me, world, blockX, blockZ, effect, color);
    }

    public static void showPillar(Player player, World world, int blockX, int blockZ, Particle effect, ParticleColor color) {
        // Lets start at the player's Y spot -30 to optimize
        for (int blockY = player.getLocation().getBlockY() - 30; blockY < player.getLocation().getBlockY() + 30; blockY++) {
            Location loc = new Location(world, blockX, blockY, blockZ);
            if (loc.getBlock().getType() != Material.AIR) {
                continue;
            }

            if (effect != null) {
                if (color == null) {
                    FactionsPlugin.getInstance().getParticleProvider().playerSpawn(player, effect, loc, 1);
                } else {
                    FactionsPlugin.getInstance().getParticleProvider().playerSpawn(player, effect, loc, color);
                }
            } else {
                Material mat = blockY % 5 == 0 ? MaterialDb.get("REDSTONE_LAMP") : MaterialDb.get("GLASS_PANE");
                VisualizeUtil.addLocation(player, loc, mat);
            }
        }
    }

}
