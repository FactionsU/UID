package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
public interface Board {
    static Board board() {
        return Instances.BOARD;
    }

    Faction factionAt(FLocation flocation);

    void claim(FLocation flocation, Faction faction);

    Set<FLocation> allClaims(Faction faction);

    void unclaim(FLocation flocation);

    void unclaimAll(Faction faction);

    void unclaimAllInWorld(Faction faction, World world);

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    default boolean isBorderLocation(FLocation flocation) {
        Faction faction = factionAt(flocation);
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction != factionAt(a) || faction != factionAt(b) || faction != factionAt(c) || faction != factionAt(d);
    }

    default boolean isConnectedLocation(FLocation flocation, Faction faction) {
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction == factionAt(a) || faction == factionAt(b) || faction == factionAt(c) || faction == factionAt(d);
    }

    default boolean isDisconnectedLocation(FLocation flocation, Faction faction) {
        FLocation a = flocation.getRelative(1, 0);
        FLocation b = flocation.getRelative(-1, 0);
        FLocation c = flocation.getRelative(0, 1);
        FLocation d = flocation.getRelative(0, -1);
        return faction != factionAt(a) && faction != factionAt(b) && faction != factionAt(c) && faction != factionAt(d);
    }

    /**
     * Checks if there is another faction within a given radius other than Wilderness. Used for HCF feature that
     * requires a 'buffer' between factions.
     *
     * @param flocation - center location.
     * @param faction   - faction checking for.
     * @param radius    - chunk radius to check.
     * @return true if another Faction is within the radius, otherwise false.
     */
    default boolean hasFactionWithin(FLocation flocation, Faction faction, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                FLocation relative = flocation.getRelative(x, z);
                Faction other = factionAt(relative);

                if (other.isNormal() && other != faction) {
                    return true;
                }
            }
        }
        return false;
    }

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    int claimCount(Faction faction);

    int claimCount(Faction faction, World world);
}
