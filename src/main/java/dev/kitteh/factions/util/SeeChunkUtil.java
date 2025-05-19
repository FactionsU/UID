package dev.kitteh.factions.util;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.particle.ParticleColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
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
        String effectName = FactionsPlugin.instance().conf().commands().seeChunk().getParticleName();
        this.effect = FactionsPlugin.instance().particleProvider().effectFromString(effectName);
        this.useColor = FactionsPlugin.instance().conf().commands().seeChunk().isRelationalColor();

        AbstractFactionsPlugin.getInstance().getLogger().info(AbstractFactionsPlugin.getInstance().txt().parse("Using %s as the ParticleEffect for /f sc", FactionsPlugin.instance().particleProvider().effectName(effect)));
    }

    @Override
    public void run() {
        for (UUID playerId : playersSeeingChunks) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                playersSeeingChunks.remove(playerId);
                continue;
            }
            if (!WorldUtil.isEnabled(player)) {
                continue;
            }
            FPlayer fme = FPlayers.fPlayers().get(player);
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
        int chunkX = flocation.x();
        int chunkZ = flocation.z();

        ParticleColor color = null;
        if (useColor) {
            ChatColor chatColor = Board.board().factionAt(flocation).relationTo(fme).chatColor();
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
        // Let's start at the player's Y spot -30 to optimize
        for (int blockY = player.getLocation().getBlockY() - 30; blockY < player.getLocation().getBlockY() + 30; blockY++) {
            Location loc = new Location(world, blockX, blockY, blockZ);
            if (loc.getBlock().getType() != Material.AIR) {
                continue;
            }

            if (color == null) {
                FactionsPlugin.instance().particleProvider().playerSpawn(player, effect, loc, 1);
            } else {
                FactionsPlugin.instance().particleProvider().playerSpawn(player, effect, loc, color);
            }
        }
    }
}
