package com.massivecraft.factions.data;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.TL;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MemoryFactions extends Factions {
    public final Map<Integer, Faction> factions = new ConcurrentHashMap<>();
    public int nextId = 1;
    private final static int ID_WILDERNESS = 0;
    private final static int ID_SAFEZONE = -1;
    private final static int ID_WARZONE = -2;

    public int load() {
        // Make sure the default neutral faction exists
        if (!factions.containsKey(ID_WILDERNESS)) {
            Faction faction = generateFactionObject(ID_WILDERNESS);
            factions.put(ID_WILDERNESS, faction);
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
        if (!factions.containsKey(ID_SAFEZONE)) {
            Faction faction = generateFactionObject(ID_SAFEZONE);
            factions.put(ID_SAFEZONE, faction);
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
        if (!factions.containsKey(ID_WARZONE)) {
            Faction faction = generateFactionObject(ID_WARZONE);
            factions.put(ID_WARZONE, faction);
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

    @Deprecated
    public Faction getFactionById(String id) {
        return factions.get(Integer.parseInt(id));
    }

    public Faction getFactionById(int id) {
        return factions.get(id);
    }

    public abstract Faction generateFactionObject(int id);

    public Faction getByTag(String str) {
        String compStr = MiscUtil.getComparisonString(str);
        for (Faction faction : factions.values()) {
            if (faction.getComparisonTag().equals(compStr)) {
                return faction;
            }
        }
        return null;
    }

    public Faction getBestTagMatch(String start) {
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

    public boolean isTagTaken(String str) {
        return this.getByTag(str) != null;
    }

    public boolean isValidFactionId(String id) {
        return this.isValidFactionId(Integer.parseInt(id));
    }

    public boolean isValidFactionId(int id) {
        return factions.containsKey(id);
    }

    public Faction createFaction() {
        Faction faction = generateFactionObject();
        factions.put(faction.getIntId(), faction);
        return faction;
    }

    public Set<String> getFactionTags() {
        Set<String> tags = new HashSet<>();
        for (Faction faction : factions.values()) {
            tags.add(faction.getTag());
        }
        return tags;
    }

    public abstract Faction generateFactionObject();

    public void removeFaction(String id) {
        factions.remove(Integer.parseInt(id)).remove();
    }

    public void removeFaction(Faction faction) {
        factions.remove(faction.getIntId()).remove();
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

    public abstract void convertFrom(MemoryFactions old);
}
