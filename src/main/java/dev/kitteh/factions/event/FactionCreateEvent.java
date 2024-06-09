package dev.kitteh.factions.event;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a Faction is created.
 */
public class FactionCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String factionTag;
    private final Player sender;
    private final Faction faction;

    public FactionCreateEvent(Player sender, String tag, Faction faction) {
        this.factionTag = tag;
        this.sender = sender;
        this.faction = faction;
    }

    public FPlayer getFPlayer() {
        return FPlayers.getInstance().getByPlayer(sender);
    }

    @Deprecated
    public String getFactionTag() {
        return factionTag;
    }

    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
