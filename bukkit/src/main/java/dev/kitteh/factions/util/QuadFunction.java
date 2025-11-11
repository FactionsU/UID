package dev.kitteh.factions.util;

@Deprecated(forRemoval = true, since = "4.3.0")
@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {
    R apply(T t, U u, V v, W w);
}
