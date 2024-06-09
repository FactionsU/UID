package dev.kitteh.factions;

import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.data.json.JSONBoard;
import dev.kitteh.factions.data.json.JSONFPlayers;
import dev.kitteh.factions.data.json.JSONFactions;

class Instances {
    static final MemoryBoard BOARD = new JSONBoard();
    static final MemoryFactions FACTIONS = new JSONFactions();
    static final MemoryFPlayers PLAYERS = new JSONFPlayers();
}
