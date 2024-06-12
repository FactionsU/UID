package dev.kitteh.factions;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record FLocation(String worldName, int x, int z) {
    //----------------------------------------------//
    // Constructors
    //----------------------------------------------//

    public FLocation() {
        this("world", 0, 0);
    }

    public FLocation(@NonNull Location location) {
        this(location.getWorld().getName(), blockToChunk(location.getBlockX()), blockToChunk(location.getBlockZ()));
    }

    public FLocation(@NonNull Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public FLocation(@NonNull Player player) {
        this(player.getLocation());
    }

    public FLocation(@NonNull Block block) {
        this(block.getChunk());
    }

    //----------------------------------------------//
    // Getters and Setters
    //----------------------------------------------//

    public @NonNull String getWorldName() {
        return worldName;
    }

    public @NonNull World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    /**
     * Returns the chunk x value, a comma, and the chunk z value, without spaces.
     *
     * @return
     */
    public @NonNull String getCoordString() {
        return x + "," + z; // Do not change without fixing usages, because it is used for serialization.
    }

    public @NonNull Chunk getChunk() {
        return getWorld().getChunkAt(x, z);
    }

    @Override
    public String toString() {
        return "[" + this.getWorldName() + "," + this.getCoordString() + "]";
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

    public @NonNull FLocation getRelative(int dx, int dz) {
        return new FLocation(this.worldName, this.x + dx, this.z + dz);
    }

    public boolean isInChunk(@NonNull Location loc) {
        return loc.getWorld().getName().equalsIgnoreCase(getWorldName()) && blockToChunk(loc.getBlockX()) == x && blockToChunk(loc.getBlockZ()) == z;
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
        if (!(obj instanceof FLocation that)) {
            return false;
        }

        return this.x == that.x && this.z == that.z && (Objects.equals(this.worldName, that.worldName));
    }
}
