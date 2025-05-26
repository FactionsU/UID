package dev.kitteh.factions.command.defaults.top;

import dev.kitteh.factions.Faction;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class FTopFacValPair implements Comparable<FTopFacValPair> {
    public final Faction faction;
    public final FTopValue value;

    public FTopFacValPair(Faction faction, FTopValue<?> value) {
        this.faction = faction;
        this.value = value;
    }

    @Override
    public int compareTo(FTopFacValPair fTopFacValPair) {
        return this.value.compareTo(fTopFacValPair.value);
    }
}
