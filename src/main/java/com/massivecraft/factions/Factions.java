package com.massivecraft.factions;

import com.massivecraft.factions.data.json.JSONFactions;

import java.util.ArrayList;
import java.util.Set;

public abstract class Factions {
    protected static Factions instance = getFactionsImpl();

    public abstract Faction getFactionById(String id);

    public abstract Faction getByTag(String str);

    public abstract Faction getBestTagMatch(String start);

    public abstract boolean isTagTaken(String str);

    public abstract boolean isValidFactionId(String id);

    public abstract Faction createFaction();

    public abstract void removeFaction(String id);

    public abstract Set<String> getFactionTags();

    public abstract ArrayList<Faction> getAllFactions();

    @Deprecated
    public Faction getNone() {
        return getWilderness();
    }

    public abstract Faction getWilderness();

    public abstract Faction getSafeZone();

    public abstract Faction getWarZone();

    public abstract void forceSave();

    public abstract void forceSave(boolean sync);

    public static Factions getInstance() {
        return instance;
    }

    private static Factions getFactionsImpl() {
        // TODO switch on configuration backend
        return new JSONFactions();
    }

    public abstract int load();
}
