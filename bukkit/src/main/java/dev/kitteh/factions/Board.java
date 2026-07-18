package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@ApiStatus.AvailableSince("4.0.0")
@ApiStatus.NonExtendable
@NullMarked
public interface Board {
    static Board board() {
        return Instances.BOARD;
    }

    Faction factionAt(FLocation location);

    void claim(FLocation location, Faction faction);

    Set<FLocation> allClaims(Faction faction);

    void unclaim(FLocation location);

    void unclaimAll(Faction faction);

    void unclaimAllInWorld(Faction faction, World world);

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    default boolean isBorderLocation(FLocation location) {
        Faction faction = factionAt(location);
        FLocation a = location.relative(1, 0);
        FLocation b = location.relative(-1, 0);
        FLocation c = location.relative(0, 1);
        FLocation d = location.relative(0, -1);
        return faction != factionAt(a) || faction != factionAt(b) || faction != factionAt(c) || faction != factionAt(d);
    }

    default boolean isConnectedLocation(FLocation location, Faction faction) {
        FLocation a = location.relative(1, 0);
        FLocation b = location.relative(-1, 0);
        FLocation c = location.relative(0, 1);
        FLocation d = location.relative(0, -1);
        return faction == factionAt(a) || faction == factionAt(b) || faction == factionAt(c) || faction == factionAt(d);
    }

    default boolean isDisconnectedLocation(FLocation location, Faction faction) {
        FLocation a = location.relative(1, 0);
        FLocation b = location.relative(-1, 0);
        FLocation c = location.relative(0, 1);
        FLocation d = location.relative(0, -1);
        return faction != factionAt(a) && faction != factionAt(b) && faction != factionAt(c) && faction != factionAt(d);
    }

    /**
     * Checks if there is another faction within a given radius other than Wilderness. Used for HCF feature that
     * requires a 'buffer' between factions.
     *
     * @param location - center location.
     * @param faction   - faction checking for.
     * @param radius    - chunk radius to check.
     * @return true if another Faction is within the radius, otherwise false.
     */
    default boolean hasFactionWithin(FLocation location, Faction faction, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                FLocation relative = location.relative(x, z);
                Faction other = factionAt(relative);

                if (other.isNormal() && other != faction) {
                    return true;
                }
            }
        }
        return false;
    }

    int claimCount(Faction faction);

    int claimCount(Faction faction, World world);

    @ApiStatus.AvailableSince("4.7.0")
    long cachedInhabitedTime(FLocation location);
}
