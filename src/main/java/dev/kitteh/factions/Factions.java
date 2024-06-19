package dev.kitteh.factions;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface Factions {
    int ID_WILDERNESS = 0;
    int ID_SAFEZONE = -1;
    int ID_WARZONE = -2;

    static Factions getInstance() {
        return Instances.FACTIONS;
    }

    @Nullable
    Faction getFactionById(String id);

    @Nullable
    Faction getByTag(String str);

    @Nullable
    Faction getBestTagMatch(String start);

    default boolean isTagTaken(String str) {
        return this.getByTag(str) != null;
    }

    Faction createFaction();

    void removeFaction(Faction faction);

    List<Faction> getAllFactions();

    @SuppressWarnings("DataFlowIssue")
    default Faction getWilderness() {
        return this.getFactionById(ID_WILDERNESS);
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction getSafeZone() {
        return this.getFactionById(ID_SAFEZONE);
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction getWarZone() {
        return this.getFactionById(ID_WARZONE);
    }
}
