package dev.kitteh.factions.util;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class Pair<Left, Right> {
    public static <Left, Right> Pair<Left, Right> of(Left left, Right right) {
        return new Pair<>(left, right);
    }

    private final Left left;
    private final Right right;

    private Pair(Left left, Right right) {
        this.left = left;
        this.right = right;
    }

    public Left getLeft() {
        return this.left;
    }

    public Right getRight() {
        return this.right;
    }
}
