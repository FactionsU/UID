package com.massivecraft.factions.event;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a Faction is soon to be created.
 */
public class FactionAttemptCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final String factionTag;
    private final Player sender;
    private boolean cancelled;

    public FactionAttemptCreateEvent(Player sender, String tag) {
        this.factionTag = tag;
        this.sender = sender;
    }

    public FPlayer getFPlayer() {
        return FPlayers.getInstance().getByPlayer(sender);
    }

    @Deprecated
    public String getFactionTag() {
        return factionTag;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
