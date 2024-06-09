package dev.kitteh.factions;

import dev.kitteh.factions.data.json.JSONFactions;

import java.util.ArrayList;
import java.util.Set;

public interface Factions {
    Faction getFactionById(String id);

    Faction getByTag(String str);

    Faction getBestTagMatch(String start);

    boolean isTagTaken(String str);

    boolean isValidFactionId(String id);

    Faction createFaction();

    void removeFaction(String id);

    Set<String> getFactionTags();

    ArrayList<Faction> getAllFactions();

    Faction getWilderness();

    Faction getSafeZone();

    Faction getWarZone();

    void forceSave();

    void forceSave(boolean sync);

    static Factions getInstance() {
        return Instances.FACTIONS;
    }

    int load();
}
