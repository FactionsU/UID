package dev.kitteh.factions.integration;

import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public final class ExternalChecks {
    private record SingleCheck(Plugin plugin, Predicate<Player> function) {
    }

    private record DoubleCheck(Plugin plugin, BiPredicate<Player, Player> function) {
    }

    private static final List<SingleCheck> afk = new ArrayList<>();
    private static final List<DoubleCheck> ignored = new ArrayList<>();
    private static final List<SingleCheck> muted = new ArrayList<>();
    private static final List<SingleCheck> vanished = new ArrayList<>();

    /**
     * Registers a function for testing if a player is AFK.
     *
     * @param plugin plugin registering
     * @param function function testing the player
     */
    public static void registerAfk(Plugin plugin, Predicate<Player> function) {
        afk.add(new SingleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    /**
     * Registers a function for testing if a player is AFK.
     *
     * @param plugin plugin registering
     * @param function function testing if, respectively, the viewer is ignoring the chatter
     */
    public static void registerIgnored(Plugin plugin, BiPredicate<Player, Player> function) {
        ignored.add(new DoubleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    /**
     * Registers a function for testing if a player is muted.
     *
     * @param plugin plugin registering
     * @param function function testing the player
     */
    @ApiStatus.AvailableSince("4.2.0")
    public static void registerMuted(Plugin plugin, Predicate<Player> function) {
        muted.add(new SingleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    /**
     * Registers a function for testing if a player is vanished.
     *
     * @param plugin plugin registering
     * @param function function testing the player
     */
    public static void registerVanished(Plugin plugin, Predicate<Player> function) {
        vanished.add(new SingleCheck(Objects.requireNonNull(plugin), Objects.requireNonNull(function)));
    }

    public static boolean isAfk(Player player) {
        for (SingleCheck check : afk) {
            try {
                if (check.function.test(player)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is afk!", e);
            }
        }
        return false;
    }

    public static boolean isIgnored(Player viewer, Player chatter) {
        for (DoubleCheck check : ignored) {
            try {
                if (check.function.test(viewer, chatter)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is ignored!", e);
            }
        }
        return false;
    }

    @ApiStatus.AvailableSince("4.2.0")
    public static boolean isMuted(Player player) {
        for (SingleCheck check : muted) {
            try {
                if (check.function.test(player)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is muted!", e);
            }
        }
        return false;
    }

    public static boolean isVanished(Player player) {
        for (SingleCheck check : vanished) {
            try {
                if (check.function.test(player)) {
                    return true;
                }
            } catch (Exception e) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.WARNING, "Could not check with " + check.plugin.getName() + " if player is vanished!", e);
            }
        }
        return false;
    }
}
