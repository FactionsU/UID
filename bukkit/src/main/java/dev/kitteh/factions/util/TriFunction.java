package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("4.3.0")
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
