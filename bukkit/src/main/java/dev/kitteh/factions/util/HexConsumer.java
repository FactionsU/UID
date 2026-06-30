package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@FunctionalInterface
public interface HexConsumer<T, U, V, W, X, Y> {
    void accept(T t, U u, V v, W w, X x, Y y);
}