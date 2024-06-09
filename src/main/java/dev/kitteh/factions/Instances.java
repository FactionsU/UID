package dev.kitteh.factions;

import dev.kitteh.factions.data.json.JSONBoard;
import dev.kitteh.factions.data.json.JSONFPlayers;
import dev.kitteh.factions.data.json.JSONFactions;

class Instances {
    static final Board BOARD = new JSONBoard();
    static final Factions FACTIONS = new JSONFactions();
    static final FPlayers PLAYERS = new JSONFPlayers();
}
