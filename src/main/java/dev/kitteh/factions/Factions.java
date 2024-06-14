package dev.kitteh.factions;

import dev.kitteh.factions.data.MemoryFactions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface Factions {
    static Factions getInstance() {
        return Instances.FACTIONS;
    }

    @Nullable Faction getFactionById(String id);

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
        return this.getFactionById("0");
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction getSafeZone() {
        return this.getFactionById("-1");
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction getWarZone() {
        return this.getFactionById("-2");
    }
}
