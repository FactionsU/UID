package dev.kitteh.factions.cmd.top;

import dev.kitteh.factions.Faction;

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
