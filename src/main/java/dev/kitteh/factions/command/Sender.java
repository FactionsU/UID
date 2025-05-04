package dev.kitteh.factions.command;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Sender {
    record Player(CommandSender sender, org.bukkit.entity.Player player, FPlayer fPlayer, Faction faction) implements Sender {
    }

    record Console(CommandSender sender) implements Sender {
    }

    static Sender of(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player player) {
            FPlayer fp = FPlayers.getInstance().getByPlayer(player);
            return new Player(sender, player, fp, fp.getFaction());
        } else {
            return new Console(sender);
        }
    }

    CommandSender sender();

    default @Nullable FPlayer fPlayerOrNull() {
        return this instanceof Player p ? p.fPlayer : null;
    }

    default void msg(TL translation, Object... args) {
        sender().sendMessage(AbstractFactionsPlugin.getInstance().txt().parse(translation.toString(), args));
    }

    default boolean hasPermission(Permission perm) {
        return sender().hasPermission(perm.node);
    }

    default boolean isPlayer() {
        return this instanceof Player;
    }

    default boolean hasFaction() {
        return this instanceof Player player && player.faction.isNormal();
    }

    default boolean isBypass() {
        return this instanceof Console || this instanceof Player player && player.fPlayer.isAdminBypassing();
    }

    default boolean isAtLeastRole(Role role) {
        return this instanceof Player player && player.faction.isNormal() && player.fPlayer.getRole().isAtLeast(role);
    }

    default void sendMessage(ComponentLike component) {
        ComponentDispatcher.send(sender(), component);
    }

    default boolean payForCommand(double cost, TL toDoThis, TL forDoingThis) {
        if (!Econ.shouldBeUsed() || cost == 0.0) {
            return true;
        }
        if (this instanceof Player player) {
            FPlayer fPlayer = player.fPlayer;
            if (fPlayer.isAdminBypassing()) {
                return true;
            }

            if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysCosts() && fPlayer.hasFaction() && fPlayer.getFaction().hasAccess(fPlayer, PermissibleActions.ECONOMY, fPlayer.getLastStoodAt())) {
                return Econ.modifyMoney(fPlayer.getFaction(), -cost, toDoThis.toString(), forDoingThis.toString());
            } else {
                return Econ.modifyMoney(fPlayer, -cost, toDoThis.toString(), forDoingThis.toString());
            }
        } else {
            return true;
        }
    }

    default boolean canAffordCommand(double cost, TL toDoThis) {
        if (!Econ.shouldBeUsed() || cost == 0.0) {
            return true;
        }
        if (this instanceof Player player) {
            FPlayer fPlayer = player.fPlayer;
            if (fPlayer.isAdminBypassing()) {
                return true;
            }
            if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysCosts() && fPlayer.hasFaction()) {
                return Econ.hasAtLeast(fPlayer.getFaction(), cost, toDoThis.toString());
            } else {
                return Econ.hasAtLeast(fPlayer, cost, toDoThis.toString());
            }
        } else {
            return true;
        }
    }
}
