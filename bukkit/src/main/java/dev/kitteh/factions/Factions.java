package dev.kitteh.factions;

import dev.kitteh.factions.plugin.Instances;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface Factions {
    int ID_WILDERNESS = 0;
    int ID_SAFEZONE = -1;
    int ID_WARZONE = -2;

    static Factions factions() {
        return Instances.FACTIONS;
    }

    @Nullable
    Faction get(int id);

    @Nullable
    Faction get(String tag);

    default Faction create(String tag) {
        return this.create(null, tag);
    }

    Faction create(@Nullable FPlayer sender, String tag);

    void remove(Faction faction);

    List<Faction> all();

    @SuppressWarnings("DataFlowIssue")
    default Faction wilderness() {
        return this.get(ID_WILDERNESS);
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction safeZone() {
        return this.get(ID_SAFEZONE);
    }

    @SuppressWarnings("DataFlowIssue")
    default Faction warZone() {
        return this.get(ID_WARZONE);
    }
}
