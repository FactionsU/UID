package com.massivecraft.factions.cmd;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.perms.PermissibleActions;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

public class CmdKick extends FCommand {

    public CmdKick() {
        super();
        this.aliases.add("kick");

        this.optionalArgs.put("player", "player");

        this.requirements = new CommandRequirements.Builder(Permission.KICK)
                .memberOnly()
                .withAction(PermissibleActions.KICK)
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer toKick = context.argIsSet(0) ? context.argAsBestFPlayerMatch(0) : null;
        if (toKick == null) {
            LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
            Component component = legacy.deserialize(TL.COMMAND_KICK_CANDIDATES.toString()).color(NamedTextColor.GOLD);
            for (FPlayer player : context.faction.getFPlayersWhereRole(Role.NORMAL)) {
                String s = player.getName();
                component = component.append(Component.text().color(NamedTextColor.WHITE).content(s + " ")
                        .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                        .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " kick " + s))
                );
            }
            if (context.fPlayer.getRole().isAtLeast(Role.COLEADER)) {
                // For both coleader and admin, add mods.
                for (FPlayer player : context.faction.getFPlayersWhereRole(Role.MODERATOR)) {
                    String s = player.getName();
                    component = component.append(Component.text().color(NamedTextColor.GRAY).content(s + " ")
                            .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                            .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " kick " + s))
                    );
                }
                if (context.fPlayer.getRole() == Role.ADMIN) {
                    // Only add coleader to this for the leader.
                    for (FPlayer player : context.faction.getFPlayersWhereRole(Role.COLEADER)) {
                        String s = player.getName();
                        component = component.append(Component.text().color(NamedTextColor.RED).content(s + " ")
                                .hoverEvent(legacy.deserialize(TL.COMMAND_KICK_CLICKTOKICK + s).asHoverEvent())
                                .clickEvent(ClickEvent.runCommand("/" + FactionsPlugin.getInstance().conf().getCommandBase().getFirst() + " kick " + s))
                        );
                    }
                }
            }

            FactionsPlugin.getInstance().getAdventure().player(context.player).sendMessage(component);
            return;
        }

        if (context.fPlayer == toKick) {
            context.msg(TL.COMMAND_KICK_SELF);
            context.msg(TL.GENERIC_YOUMAYWANT + FCmdRoot.getInstance().cmdLeave.getUsageTemplate(context));
            return;
        }

        Faction toKickFaction = toKick.getFaction();

        if (toKickFaction.isWilderness()) {
            context.sender.sendMessage(TL.COMMAND_KICK_NONE.toString());
            return;
        }

        // players with admin-level "disband" permission can bypass these requirements
        if (!Permission.KICK_ANY.has(context.sender)) {
            if (toKickFaction != context.faction) {
                context.msg(TL.COMMAND_KICK_NOTMEMBER, toKick.describeTo(context.fPlayer, true), context.faction.describeTo(context.fPlayer));
                return;
            }

            if (toKick.getRole().value >= context.fPlayer.getRole().value) {
                context.msg(TL.COMMAND_KICK_INSUFFICIENTRANK);
                return;
            }

            if (!FactionsPlugin.getInstance().getLandRaidControl().canKick(toKick, context)) {
                return;
            }
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.canAffordCommand(FactionsPlugin.getInstance().conf().economy().getCostKick(), TL.COMMAND_KICK_TOKICK.toString())) {
            return;
        }

        // trigger the leave event (cancellable) [reason:kicked]
        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.getFaction(), FPlayerLeaveEvent.PlayerLeaveReason.KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostKick(), TL.COMMAND_KICK_TOKICK.toString(), TL.COMMAND_KICK_FORKICK.toString())) {
            return;
        }

        toKickFaction.msg(TL.COMMAND_KICK_FACTION, context.fPlayer.describeTo(toKickFaction, true), toKick.describeTo(toKickFaction, true));
        toKick.msg(TL.COMMAND_KICK_KICKED, context.fPlayer.describeTo(toKick, true), toKickFaction.describeTo(toKick));
        if (toKickFaction != context.faction) {
            context.msg(TL.COMMAND_KICK_KICKS, toKick.describeTo(context.fPlayer), toKickFaction.describeTo(context.fPlayer));
        }

        if (FactionsPlugin.getInstance().conf().logging().isFactionKick()) {
            FactionsPlugin.getInstance().log((context.player == null ? "A console command" : context.fPlayer.getName()) + " kicked " + toKick.getName() + " from the faction: " + toKickFaction.getTag());
        }

        if (toKick.getRole() == Role.ADMIN) {
            toKickFaction.promoteNewLeader();
        }

        toKickFaction.deinvite(toKick);
        toKick.resetFactionData();
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_KICK_DESCRIPTION;
    }

}
