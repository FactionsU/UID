package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Deprecated(forRemoval = true, since = "4.3.0")
@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {
    R apply(T t, U u, V v, W w);
}
