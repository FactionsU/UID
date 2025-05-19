package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdKick implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("kick")
                            .commandDescription(Cloudy.desc(TL.COMMAND_KICK_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.KICK).and(Cloudy.hasSelfFactionPerms(PermissibleActions.KICK))))
                            .optional("target", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        FPlayer toKick = context.getOrDefault("target", null);

        if (toKick == null) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(TL.COMMAND_KICK_CANDIDATES.toString()).color(NamedTextColor.GOLD);
            for (FPlayer player : faction.members(Role.RECRUIT)) {
                String s = player.name();
                component = component.append(Component.text().color(NamedTextColor.WHITE).content(s + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.instance().conf().getCommandBase().getFirst() + " kick " + s))
                );
            }
            for (FPlayer player : faction.members(Role.NORMAL)) {
                String s = player.name();
                component = component.append(Component.text().color(NamedTextColor.WHITE).content(s + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.instance().conf().getCommandBase().getFirst() + " kick " + s))
                );
            }
            if (sender.role().isAtLeast(Role.COLEADER)) {
                // For both coleader and admin, add mods.
                for (FPlayer player : faction.members(Role.MODERATOR)) {
                    String s = player.name();
                    component = component.append(Component.text().color(NamedTextColor.GRAY).content(s + " ")
                            .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                            .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.instance().conf().getCommandBase().getFirst() + " kick " + s))
                    );
                }
                if (sender.role() == Role.ADMIN) {
                    // Only add coleader to this for the leader.
                    for (FPlayer player : faction.members(Role.COLEADER)) {
                        String s = player.name();
                        component = component.append(Component.text().color(NamedTextColor.RED).content(s + " ")
                                .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                                .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.instance().conf().getCommandBase().getFirst() + " kick " + s))
                        );
                    }
                }
            }

            context.sender().sendMessage(component);
            return;
        }

        if (sender == toKick) {
            sender.msg(TL.COMMAND_KICK_SELF);
            return;
        }

        Faction toKickFaction = toKick.faction();

        if (toKickFaction.isWilderness()) {
            sender.sendMessage(TL.COMMAND_KICK_NONE.toString());
            return;
        }

        if (toKickFaction != faction) {
            sender.msg(TL.COMMAND_KICK_NOTMEMBER, toKick.describeTo(sender, true), faction.describeTo(sender));
            return;
        }

        if (toKick.role().isAtLeast(sender.role())) {
            sender.msg(TL.COMMAND_KICK_INSUFFICIENTRANK);
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canKick(toKick, sender)) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.instance().conf().economy().getCostKick(), TL.COMMAND_KICK_TOKICK)) {
            return;
        }

        // trigger the leave event (cancellable) [reason:kicked]
        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.faction(), FPlayerLeaveEvent.Reason.KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostKick(), TL.COMMAND_KICK_TOKICK, TL.COMMAND_KICK_FORKICK)) {
            return;
        }

        toKickFaction.msg(TL.COMMAND_KICK_FACTION, sender.describeTo(toKickFaction, true), toKick.describeTo(toKickFaction, true));
        toKick.msg(TL.COMMAND_KICK_KICKED, sender.describeTo(toKick, true), toKickFaction.describeTo(toKick));

        if (FactionsPlugin.instance().conf().logging().isFactionKick()) {
            FactionsPlugin.instance().log(sender.name() + " kicked " + toKick.name() + " from the faction: " + toKickFaction.tag());
        }

        toKickFaction.deInvite(toKick);
        toKick.resetFactionData(true);
    }
}
