package dev.kitteh.factions.command;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public interface Sender {
    interface Player extends Sender {
        record Impl(CommandSender sender, org.bukkit.entity.Player player, FPlayer fPlayer, Faction faction) implements Player {
        }

        org.bukkit.entity.Player player();

        FPlayer fPlayer();

        Faction faction();
    }

    interface Console extends Sender {
        record Impl(CommandSender sender) implements Console {
        }
    }

    CommandSender sender();

    default @Nullable FPlayer fPlayerOrNull() {
        return this instanceof Player p ? p.fPlayer() : null;
    }

    default void msg(TL translation, Object... args) {
        sender().sendMessage(TextUtil.parse(translation.toString(), args));
    }

    default boolean hasPermission(Permission perm) {
        return sender().hasPermission(perm.node);
    }

    default boolean isPlayer() {
        return this instanceof Player;
    }

    default boolean hasFaction() {
        return this instanceof Player player && player.faction().isNormal();
    }

    default boolean isBypass() {
        return this instanceof Console || this instanceof Player player && player.fPlayer().adminBypass();
    }

    default boolean isAtLeastRole(Role role) {
        return this instanceof Player player && player.faction().isNormal() && player.fPlayer().role().isAtLeast(role);
    }

    default void sendMessage(ComponentLike component) {
        ComponentDispatcher.send(sender(), component);
    }

    default void sendRichMessage(String miniMessage, TagResolver... resolvers) {
        this.sendMessage(Mini.parse(miniMessage, resolvers));
    }

    default boolean payForCommand(double cost, TL toDoThis, TL forDoingThis) {
        if (!Econ.shouldBeUsed() || cost == 0.0) {
            return true;
        }
        if (this instanceof Player player) {
            FPlayer fPlayer = player.fPlayer();
            if (fPlayer.adminBypass()) {
                return true;
            }

            if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts() && fPlayer.hasFaction() && fPlayer.faction().hasAccess(fPlayer, PermissibleActions.ECONOMY, fPlayer.lastStoodAt())) {
                return Econ.modifyMoney(fPlayer.faction(), -cost, toDoThis.toString(), forDoingThis.toString());
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
            FPlayer fPlayer = player.fPlayer();
            if (fPlayer.adminBypass()) {
                return true;
            }
            if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysCosts() && fPlayer.hasFaction()) {
                return Econ.hasAtLeast(fPlayer.faction(), cost, toDoThis.toString());
            } else {
                return Econ.hasAtLeast(fPlayer, cost, toDoThis.toString());
            }
        } else {
            return true;
        }
    }
}
