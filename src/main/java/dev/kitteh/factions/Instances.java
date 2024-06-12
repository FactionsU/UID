package dev.kitteh.factions;

import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.data.json.JSONBoard;
import dev.kitteh.factions.data.json.JSONFPlayers;
import dev.kitteh.factions.data.json.JSONFactions;
import org.jspecify.annotations.NonNull;

final class Instances {
    static final @NonNull MemoryBoard BOARD = new JSONBoard();
    static final @NonNull MemoryFactions FACTIONS = new JSONFactions();
    static final @NonNull MemoryFPlayers PLAYERS = new JSONFPlayers();
}
