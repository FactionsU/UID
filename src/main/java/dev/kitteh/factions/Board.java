package dev.kitteh.factions;

import dev.kitteh.factions.data.json.JSONBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

import java.util.List;
import java.util.Set;


public interface Board {
    String getIdAt(FLocation flocation);

    static Board getInstance() {
        return Instances.BOARD;
    }

    Faction getFactionAt(FLocation flocation);

    void setIdAt(String id, FLocation flocation);

    void setFactionAt(Faction faction, FLocation flocation);

    void removeAt(FLocation flocation);

    Set<FLocation> getAllClaims(String factionId);

    Set<FLocation> getAllClaims(Faction faction);

    // not to be confused with claims, ownership referring to further member-specific ownership of a claim
    void clearOwnershipAt(FLocation flocation);

    void unclaimAll(String factionId);

    void unclaimAllInWorld(String factionId, World world);

    // Is this coord NOT completely surrounded by coords claimed by the same faction?
    // Simpler: Is there any nearby coord with a faction other than the faction here?
    boolean isBorderLocation(FLocation flocation);

    // Is this coord connected to any coord claimed by the specified faction?
    boolean isConnectedLocation(FLocation flocation, Faction faction);

    boolean hasFactionWithin(FLocation flocation, Faction faction, int radius);

    //----------------------------------------------//
    // Cleaner. Remove orphaned foreign keys
    //----------------------------------------------//

    void clean();

    //----------------------------------------------//
    // Coord count
    //----------------------------------------------//

    int getFactionCoordCount(String factionId);

    int getFactionCoordCount(Faction faction);

    int getFactionCoordCountInWorld(Faction faction, String worldName);

    //----------------------------------------------//
    // Map generation
    //----------------------------------------------//

    /**
     * The map is relative to a coord and a faction north is in the direction of decreasing x east is in the direction
     * of decreasing z
     */
    List<Component> getMap(FPlayer fPlayer, FLocation flocation, double inDegrees);

    void forceSave();

    void forceSave(boolean sync);

    int load();
}
