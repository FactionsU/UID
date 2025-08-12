package dev.kitteh.factions.plugin;

import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.data.MemoryUniverse;
import dev.kitteh.factions.data.json.JSONBoard;
import dev.kitteh.factions.data.json.JSONFPlayers;
import dev.kitteh.factions.data.json.JSONFactions;
import dev.kitteh.factions.data.json.JSONUniverse;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Instances for internal access.
 */
@ApiStatus.Internal
@NullMarked
public final class Instances {
    public static final MemoryBoard BOARD = new JSONBoard();
    public static final MemoryFactions FACTIONS = new JSONFactions();
    public static final MemoryFPlayers PLAYERS = new JSONFPlayers();
    public static final MemoryUniverse UNIVERSE = new JSONUniverse();
}
