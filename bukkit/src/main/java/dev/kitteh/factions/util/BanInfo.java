package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * @param banner banning player
 * @param banned banned player
 * @param time   duration, in millis
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public record BanInfo(UUID banner, UUID banned, long time) {
}
