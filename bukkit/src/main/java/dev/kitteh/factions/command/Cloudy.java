package dev.kitteh.factions.command;

import dev.kitteh.factions.permissible.PermissibleAction;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.permission.PredicatePermission;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.function.Predicate;

/**
 * Helper methods for cloud.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public class Cloudy {
    public static PredicatePermission<Sender> predicate(Predicate<Sender> predicate) {
        return PredicatePermission.of(predicate);
    }

    public static PredicatePermission<Sender> hasPermission(String permission) {
        return PredicatePermission.of(s -> s.sender().hasPermission(permission));
    }

    public static PredicatePermission<Sender> hasPermission(Permission permission) {
        return PredicatePermission.of(s -> s.sender().hasPermission(permission.node));
    }

    public static PredicatePermission<Sender> isPlayer() {
        return PredicatePermission.of(Sender::isPlayer);
    }

    public static PredicatePermission<Sender> hasFaction() {
        return PredicatePermission.of(Sender::hasFaction);
    }

    public static PredicatePermission<Sender> isBypass() {
        return PredicatePermission.of(Sender::isBypass);
    }

    public static PredicatePermission<Sender> isAtLeastRole(Role role) {
        return PredicatePermission.of(s -> s.isAtLeastRole(role));
    }

    public static PredicatePermission<Sender> hasSelfFactionPerms(PermissibleAction action) {
        return PredicatePermission.of(s -> s instanceof Sender.Player player && player.faction().isNormal() && player.faction().hasAccess(player.fPlayer(), action, player.fPlayer().lastStoodAt()));
    }

    public static Description desc(TL tl) {
        return Description.of(ChatColor.stripColor(tl.toString()));
    }
}
