package dev.kitteh.factions.event;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.permissible.Relation;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

/**
 * Event called when a Faction relation is called.
 */
@NullMarked
public class FactionRelationEvent extends FactionEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Faction ftarget;
    private final Relation foldrel;
    private final Relation frel;

    public FactionRelationEvent(Faction sender, Faction target, Relation oldrel, Relation rel) {
        super(sender);
        ftarget = target;
        foldrel = oldrel;
        frel = rel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Relation getOldRelation() {
        return foldrel;
    }

    public Relation getNewRelation() {
        return frel;
    }

    public Faction getTargetFaction() {
        return ftarget;
    }
}
