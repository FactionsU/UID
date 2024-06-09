package dev.kitteh.factions.struct;

import java.util.UUID;

/**
 * @param banner FPlayer IDs
 */
public record BanInfo(UUID banner, UUID banned, long time) {

    /**
     * Get the FPlayer ID of the player who issued the ban.
     *
     * @return FPlayer ID.
     */
    @Override
    public UUID banner() {
        return this.banner;
    }

    /**
     * Get the FPlayer ID of the player who got banned.
     *
     * @return FPlayer ID.
     */
    @Override
    public UUID banned() {
        return banned;
    }

    /**
     * Get the server time when the ban was issued.
     *
     * @return system timestamp.
     */
    @Override
    public long time() {
        return time;
    }
}
