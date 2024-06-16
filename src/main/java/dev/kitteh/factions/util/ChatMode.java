package dev.kitteh.factions.util;

import java.util.function.Supplier;

public enum ChatMode {
    MOD(4, TL.CHAT_MOD::toString),
    FACTION(3, TL.CHAT_FACTION::toString),
    ALLIANCE(2, TL.CHAT_ALLIANCE::toString),
    TRUCE(1, TL.CHAT_TRUCE::toString),
    PUBLIC(0, TL.CHAT_PUBLIC::toString);

    private final int value;
    private final Supplier<String> nicename;

    ChatMode(final int value, final Supplier<String> nicename) {
        this.value = value;
        this.nicename = nicename;
    }

    public boolean isAtLeast(ChatMode role) {
        return this.value >= role.value;
    }

    public boolean isAtMost(ChatMode role) {
        return this.value <= role.value;
    }

    @Override
    public String toString() {
        return this.nicename.get();
    }

    public ChatMode getNext() {
        return switch (this) {
            case PUBLIC -> TRUCE;
            case TRUCE -> ALLIANCE;
            case ALLIANCE -> FACTION;
            case FACTION -> MOD;
            default -> PUBLIC;
        };
    }
}
