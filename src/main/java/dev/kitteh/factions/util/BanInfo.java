package dev.kitteh.factions.util;

import java.util.UUID;

/**
 * @param banner banning player
 * @param banned banned player
 * @param time duration, in millis
 */
public record BanInfo(UUID banner, UUID banned, long time) {
}
