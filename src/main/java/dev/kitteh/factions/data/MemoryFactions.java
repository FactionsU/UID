package dev.kitteh.factions.data;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.event.FactionCreateEvent;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
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
            MemoryFaction faction = generateFactionObject(Factions.ID_WILDERNESS, TL.WILDERNESS.toString());
            factions.put(Factions.ID_WILDERNESS, faction);
            faction.description(TL.WILDERNESS_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_WILDERNESS);
            if (!faction.tag().equalsIgnoreCase(TL.WILDERNESS.toString())) {
                faction.tag(TL.WILDERNESS.toString());
            }
            if (!faction.description().equalsIgnoreCase(TL.WILDERNESS_DESCRIPTION.toString())) {
                faction.description(TL.WILDERNESS_DESCRIPTION.toString());
            }
        }

        // Make sure the safe zone faction exists
        if (!factions.containsKey(Factions.ID_SAFEZONE)) {
            MemoryFaction faction = generateFactionObject(Factions.ID_SAFEZONE, TL.SAFEZONE.toString());
            factions.put(Factions.ID_SAFEZONE, faction);
            faction.description(TL.SAFEZONE_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_SAFEZONE);
            if (!faction.tag().equalsIgnoreCase(TL.SAFEZONE.toString())) {
                faction.tag(TL.SAFEZONE.toString());
            }
            if (!faction.description().equalsIgnoreCase(TL.SAFEZONE_DESCRIPTION.toString())) {
                faction.description(TL.SAFEZONE_DESCRIPTION.toString());
            }
            // if SafeZone has old pre-1.6.0 name, rename it to remove troublesome " "
            if (faction.tag().contains(" ")) {
                faction.tag(TL.SAFEZONE.toString());
            }
        }

        // Make sure the war zone faction exists
        if (!factions.containsKey(Factions.ID_WARZONE)) {
            MemoryFaction faction = generateFactionObject(Factions.ID_WARZONE, TL.WARZONE.toString());
            factions.put(Factions.ID_WARZONE, faction);
            faction.description(TL.WARZONE_DESCRIPTION.toString());
        } else {
            Faction faction = factions.get(ID_WARZONE);
            if (!faction.tag().equalsIgnoreCase(TL.WARZONE.toString())) {
                faction.tag(TL.WARZONE.toString());
            }
            if (!faction.description().equalsIgnoreCase(TL.WARZONE_DESCRIPTION.toString())) {
                faction.description(TL.WARZONE_DESCRIPTION.toString());
            }
            // if WarZone has old pre-1.6.0 name, rename it to remove troublesome " "
            if (faction.tag().contains(" ")) {
                faction.tag(TL.WARZONE.toString());
            }
        }
        return 0;
    }

    public abstract void forceSave(boolean sync);

    @Override
    public @Nullable Faction get(int id) {
        return factions.get(id);
    }

    public abstract MemoryFaction generateFactionObject(int id, String tag);

    @Override
    public @Nullable Faction get(String tag) {
        String compStr = MiscUtil.getComparisonString(Objects.requireNonNull(tag));
        for (Faction faction : factions.values()) {
            if (MiscUtil.getComparisonString(faction.tag()).equals(compStr)) {
                return faction;
            }
        }
        return null;
    }

    @Override
    public Faction create(@Nullable FPlayer sender, String tag) {
        MemoryFaction faction = generateFactionObject(tag);
        factions.put(faction.id(), faction);
        Bukkit.getServer().getPluginManager().callEvent(new FactionCreateEvent(sender, faction, sender == null ? FactionCreateEvent.Reason.PLUGIN : FactionCreateEvent.Reason.COMMAND));
        return faction;
    }

    public abstract MemoryFaction generateFactionObject(String tag);

    @Override
    public void remove(Faction faction) {
        factions.remove(faction.id()).remove();
    }

    @Override
    public ArrayList<Faction> all() {
        return new ArrayList<>(factions.values());
    }

    @Override
    public Faction wilderness() {
        return factions.get(ID_WILDERNESS);
    }

    @Override
    public Faction safeZone() {
        return factions.get(ID_SAFEZONE);
    }

    @Override
    public Faction warZone() {
        return factions.get(ID_WARZONE);
    }
}
