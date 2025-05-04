package dev.kitteh.factions;

import dev.kitteh.factions.util.LazyLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public record FLocation(String worldName, int x, int z) {
    //----------------------------------------------//
    // Constructors
    //----------------------------------------------//

    public FLocation() {
        this("world", 0, 0);
    }

    public FLocation(Location location) {
        this(location.getWorld().getName(), blockToChunk(location.getBlockX()), blockToChunk(location.getBlockZ()));
    }

    public FLocation(LazyLocation location) {
        this(location.worldName(), blockToChunk((int) location.x()), blockToChunk((int) location.z()));
    }

    public FLocation(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public FLocation(Player player) {
        this(player.getLocation());
    }

    public FLocation(Block block) {
        this(block.getChunk());
    }

    //----------------------------------------------//
    // Getters and Setters
    //----------------------------------------------//

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Faction getFaction() {
        return Board.getInstance().getFactionAt(this);
    }

    /**
     * Returns the chunk x value, a comma, and the chunk z value, without spaces.
     *
     * @return
     */
    public String getCoordString() {
        return x + "," + z; // Do not change without fixing usages, because it is used for serialization.
    }

    public Chunk getChunk() {
        return getWorld().getChunkAt(x, z);
    }

    @Override
    public String toString() {
        return "[" + this.worldName() + "," + this.getCoordString() + "]";
    }

    //----------------------------------------------//
    // Block/Chunk/Region Value Transformation
    //----------------------------------------------//

    // bit-shifting is used because it's much faster than standard division and multiplication
    public static int blockToChunk(int blockVal) {    // 1 chunk is 16x16 blocks
        return blockVal >> 4;   // ">> 4" == "/ 16"
    }

    public static int chunkToBlock(int chunkVal) {
        return chunkVal << 4;   // "<< 4" == "* 16"
    }

    //----------------------------------------------//
    // Misc Geometry
    //----------------------------------------------//

    public FLocation getRelative(int dx, int dz) {
        return new FLocation(this.worldName, this.x + dx, this.z + dz);
    }

    public boolean isInChunk(Location loc) {
        return loc.getWorld().getName().equalsIgnoreCase(worldName()) && blockToChunk(loc.getBlockX()) == x && blockToChunk(loc.getBlockZ()) == z;
    }

    public boolean isInChunk(LazyLocation loc) {
        return loc.worldName().equalsIgnoreCase(worldName()) && blockToChunk(NumberConversions.floor(loc.x())) == x && blockToChunk(NumberConversions.floor(loc.z())) == z;
    }

    /**
     * Checks if the chunk represented by this FLocation is outside the world border
     *
     * @param buffer the number of chunks from the border that will be treated as "outside"
     * @return whether this location is outside of the border
     */
    public boolean isOutsideWorldBorder(int buffer) {
        WorldBorder border = getWorld().getWorldBorder();

        Location center = border.getCenter();
        double size = border.getSize();

        int bufferBlocks = buffer << 4;

        double borderMinX = (center.getX() - size / 2.0D) + bufferBlocks;
        double borderMinZ = (center.getZ() - size / 2.0D) + bufferBlocks;
        double borderMaxX = (center.getX() + size / 2.0D) - bufferBlocks;
        double borderMaxZ = (center.getZ() + size / 2.0D) - bufferBlocks;

        int chunkMinX = this.x << 4;
        int chunkMaxX = chunkMinX | 15;
        int chunkMinZ = this.z << 4;
        int chunkMaxZ = chunkMinZ | 15;

        return (chunkMinX >= borderMaxX) || (chunkMinZ >= borderMaxZ) || (chunkMaxX <= borderMinX) || (chunkMaxZ <= borderMinZ);
    }

    //----------------------------------------------//
    // Comparison
    //----------------------------------------------//

    @Override
    public int hashCode() {
        // should be fast, with good range and few hash collisions: (x * 512) + z + worldName.hashCode
        return (this.x << 9) + this.z + (this.worldName != null ? this.worldName.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FLocation(String world, int xx, int zz))) {
            return false;
        }

        return this.x == xx && this.z == zz && (Objects.equals(this.worldName, world));
    }
}
