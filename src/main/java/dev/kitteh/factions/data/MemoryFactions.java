package dev.kitteh.factions.data;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public abstract class MemoryFactions implements Factions {
    public final Map<Integer, MemoryFaction> factions = new ConcurrentHashMap<>();
    public int nextId = 1;


    public int load() {
        for (MemoryFaction faction : factions.values()) {
            faction.cleanupDeserialization();
        }

        // Make sure the default neutral faction exists
        if (!factions.containsKey(Factions.ID_WILDERNESS)) {
            MemoryFaction faction = generateFactionObject(Factions.ID_WILDERNESS);
            factions.put(Factions.ID_WILDERNESS, faction);
            faction.setTag(TL.WILDERNESS.toString());
            faction.setDescription(TL.WILDERNESS_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_WILDERNESS);
            if (!faction.getTag().equalsIgnoreCase(TL.WILDERNESS.toString())) {
                faction.setTag(TL.WILDERNESS.toString());
            }
            if (!faction.getDescription().equalsIgnoreCase(TL.WILDERNESS_DESCRIPTION.toString())) {
                faction.setDescription(TL.WILDERNESS_DESCRIPTION.toString());
            }
        }

        // Make sure the safe zone faction exists
        if (!factions.containsKey(Factions.ID_SAFEZONE)) {
            MemoryFaction faction = generateFactionObject(Factions.ID_SAFEZONE);
            factions.put(Factions.ID_SAFEZONE, faction);
            faction.setTag(TL.SAFEZONE.toString());
            faction.setDescription(TL.SAFEZONE_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_SAFEZONE);
            if (!faction.getTag().equalsIgnoreCase(TL.SAFEZONE.toString())) {
                faction.setTag(TL.SAFEZONE.toString());
            }
            if (!faction.getDescription().equalsIgnoreCase(TL.SAFEZONE_DESCRIPTION.toString())) {
                faction.setDescription(TL.SAFEZONE_DESCRIPTION.toString());
            }
            // if SafeZone has old pre-1.6.0 name, rename it to remove troublesome " "
            if (faction.getTag().contains(" ")) {
                faction.setTag(TL.SAFEZONE.toString());
            }
        }

        // Make sure the war zone faction exists
        if (!factions.containsKey(Factions.ID_WARZONE)) {
            MemoryFaction faction = generateFactionObject(Factions.ID_WARZONE);
            factions.put(Factions.ID_WARZONE, faction);
            faction.setTag(TL.WARZONE.toString());
            faction.setDescription(TL.WARZONE_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_WARZONE);
            if (!faction.getTag().equalsIgnoreCase(TL.WARZONE.toString())) {
                faction.setTag(TL.WARZONE.toString());
            }
            if (!faction.getDescription().equalsIgnoreCase(TL.WARZONE_DESCRIPTION.toString())) {
                faction.setDescription(TL.WARZONE_DESCRIPTION.toString());
            }
            // if WarZone has old pre-1.6.0 name, rename it to remove troublesome " "
            if (faction.getTag().contains(" ")) {
                faction.setTag(TL.WARZONE.toString());
            }
        }
        return 0;
    }

    public abstract void forceSave(boolean sync);

    public Faction getFactionById(String id) {
        return factions.get(Integer.parseInt(id));
    }

    public @Nullable Faction getFactionById(int id) {
        return factions.get(id);
    }

    public abstract MemoryFaction generateFactionObject(int id);

    public @Nullable Faction getByTag(String str) {
        String compStr = MiscUtil.getComparisonString(Objects.requireNonNull(str));
        for (Faction faction : factions.values()) {
            if (faction.getComparisonTag().equals(compStr)) {
                return faction;
            }
        }
        return null;
    }

    public @Nullable Faction getBestTagMatch(String start) {
        Objects.requireNonNull(start);
        int best = 0;
        start = start.toLowerCase();
        int minlength = start.length();
        Faction bestMatch = null;
        for (Faction faction : factions.values()) {
            String candidate = faction.getTag();
            candidate = ChatColor.stripColor(candidate);
            if (candidate.length() < minlength) {
                continue;
            }
            if (!candidate.toLowerCase().startsWith(start)) {
                continue;
            }

            // The closer to zero the better
            int lendiff = candidate.length() - minlength;
            if (lendiff == 0) {
                return faction;
            }
            if (lendiff < best || best == 0) {
                best = lendiff;
                bestMatch = faction;
            }
        }

        return bestMatch;
    }

    public Faction createFaction() {
        MemoryFaction faction = generateFactionObject();
        factions.put(faction.getId(), faction);
        return faction;
    }

    public abstract MemoryFaction generateFactionObject();

    public void removeFaction(Faction faction) {
        factions.remove(faction.getId()).remove();
    }

    @Override
    public ArrayList<Faction> getAllFactions() {
        return new ArrayList<>(factions.values());
    }

    @Override
    public Faction getWilderness() {
        return factions.get(ID_WILDERNESS);
    }

    @Override
    public Faction getSafeZone() {
        return factions.get(ID_SAFEZONE);
    }

    @Override
    public Faction getWarZone() {
        return factions.get(ID_WARZONE);
    }
}
