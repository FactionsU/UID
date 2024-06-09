package dev.kitteh.factions;

import java.util.Collection;
import java.util.List;

public interface Factions {
    static Factions getInstance() {
        return Instances.FACTIONS;
    }

    Faction getFactionById(String id);

    Faction getByTag(String str);

    Faction getBestTagMatch(String start);

    boolean isTagTaken(String str);

    boolean isValidFactionId(String id);

    Faction createFaction();

    void removeFaction(Faction faction);

    List<Faction> getAllFactions();

    Faction getWilderness();

    Faction getSafeZone();

    Faction getWarZone();
}
