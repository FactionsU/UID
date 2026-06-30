package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("4.0.0")
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}