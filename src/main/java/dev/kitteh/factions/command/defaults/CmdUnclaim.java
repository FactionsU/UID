package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.LandUnclaimAllEvent;
import dev.kitteh.factions.event.LandUnclaimEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

public class CmdUnclaim implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("unclaim")
                            .commandDescription(Cloudy.desc(TL.COMMAND_UNCLAIM_DESCRIPTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.UNCLAIM)
                                                    .and(
                                                            Cloudy.hasFaction()
                                                                    .or(Cloudy.hasPermission(Permission.MANAGE_SAFE_ZONE).or(Cloudy.hasPermission(Permission.MANAGE_WAR_ZONE)))
                                                    )
                                            )
                            )
                            .flag(manager.flagBuilder("faction").withComponent(FactionParser.of()))
                            .flag(
                                    manager.flagBuilder("radius")
                                            .withComponent(IntegerParser.integerParser(1))
                                            .withPermission(Cloudy.hasPermission(Permission.CLAIM_RADIUS))
                            )
                            .flag(
                                    manager.flagBuilder("fill")
                                            .withPermission(Cloudy.hasPermission(Permission.UNCLAIM_FILL))
                            )
                            .flag(
                                    manager.flagBuilder("fill-limit")
                                            .withPermission(Cloudy.hasPermission(Permission.UNCLAIM_FILL))
                                            .withComponent(IntegerParser.integerParser(1, FactionsPlugin.getInstance().conf().factions().claims().getFillUnClaimMaxClaims()))
                            )
                            .flag(
                                    manager.flagBuilder("auto")
                                            .withPermission(Cloudy.hasPermission(Permission.AUTOCLAIM))
                            )
                            .flag(
                                    manager.flagBuilder("all-territory")
                                            .withPermission(Cloudy.hasPermission(Permission.UNCLAIM_ALL).and(Cloudy.isAtLeastRole(Role.ADMIN).or(Cloudy.isBypass())))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        FLocation claimLocation = new FLocation(player);

        final Faction forFaction = context.flags().get("faction") instanceof Faction fac ? fac : sender.faction(); // Default to own

        if (context.flags().hasFlag("all-territory")) {
            this.unclaimAll(sender, forFaction, false);
            return;
        }

        if (context.flags().hasFlag("auto")) {
            if (sender.autoUnclaim() == null) {
                if (sender.canClaimForFaction(forFaction)) {
                    sender.autoUnclaim(forFaction);

                    sender.msg(TL.COMMAND_AUTOUNCLAIM_ENABLED, forFaction.describeTo(sender));
                    sender.attemptUnclaim(forFaction, claimLocation, true);
                } else {
                    sender.msg(TL.COMMAND_AUTOUNCLAIM_OTHERFACTION, forFaction.describeTo(sender));
                }
            } else {
                sender.autoUnclaim(null);
                sender.msg(TL.COMMAND_AUTOUNCLAIM_DISABLED);
            }

            return;
        }

        if (context.flags().hasFlag("fill")) {
            int limit = context.flags().get("fill-limit") instanceof Integer i ? i : FactionsPlugin.getInstance().conf().factions().claims().getFillUnClaimMaxClaims();

            this.fill(sender, claimLocation, forFaction, limit);

            return;
        }

        if (context.flags().get("radius") instanceof Integer radius && radius > 1) {
            if (!Permission.CLAIM_RADIUS.has(player)) {
                sender.msg(TL.COMMAND_CLAIM_DENIED);
                return;
            }

            new SpiralTask(claimLocation, radius) {
                private int failCount = 0;
                private final int limit = FactionsPlugin.getInstance().conf().factions().claims().getRadiusClaimFailureLimit() - 1;

                @Override
                public boolean work() {
                    boolean success = sender.attemptUnclaim(forFaction, this.currentFLocation(), true);
                    if (success) {
                        failCount = 0;
                    } else if (failCount++ >= limit) {
                        this.stop();
                        return false;
                    }

                    return true;
                }
            };

            return;
        }

        sender.attemptUnclaim(forFaction, claimLocation, true);
    }

    private void fill(FPlayer sender, FLocation loc, Faction forFaction, int limit) {
        if (limit > FactionsPlugin.getInstance().conf().factions().claims().getFillUnClaimMaxClaims()) {
            sender.msg(TL.COMMAND_UNCLAIMFILL_ABOVEMAX, FactionsPlugin.getInstance().conf().factions().claims().getFillUnClaimMaxClaims());
            return;
        }

        final boolean bypass = sender.adminBypass();

        Faction currentFaction = Board.board().factionAt(loc);

        if (currentFaction != forFaction) {
            sender.msg(TL.COMMAND_UNCLAIMFILL_NOTCLAIMED);
            return;
        }

        if (!bypass &&
                (
                        (forFaction.isNormal() && !forFaction.hasAccess(sender, PermissibleActions.TERRITORY, loc))
                                ||
                                (forFaction.isWarZone() && !Permission.MANAGE_WAR_ZONE.has(sender.asPlayer()))
                                ||
                                (forFaction.isSafeZone() && !Permission.MANAGE_SAFE_ZONE.has(sender.asPlayer()))
                )
        ) {
            sender.msg(TL.CLAIM_CANTUNCLAIM, forFaction.describeTo(sender));
            return;
        }

        final double distance = FactionsPlugin.getInstance().conf().factions().claims().getFillUnClaimMaxDistance();
        int startX = loc.x();
        int startZ = loc.z();

        Set<FLocation> toClaim = new LinkedHashSet<>();
        Queue<FLocation> queue = new LinkedList<>();
        FLocation currentHead;
        queue.add(loc);
        toClaim.add(loc);
        while (!queue.isEmpty() && toClaim.size() <= limit) {
            currentHead = queue.poll();

            if (Math.abs(currentHead.x() - startX) > distance || Math.abs(currentHead.z() - startZ) > distance) {
                sender.msg(TL.COMMAND_UNCLAIMFILL_TOOFAR, distance);
                return;
            }

            addIf(toClaim, queue, currentHead.relative(0, 1), currentFaction);
            addIf(toClaim, queue, currentHead.relative(0, -1), currentFaction);
            addIf(toClaim, queue, currentHead.relative(1, 0), currentFaction);
            addIf(toClaim, queue, currentHead.relative(-1, 0), currentFaction);
        }

        if (toClaim.size() > limit) {
            sender.msg(TL.COMMAND_UNCLAIMFILL_PASTLIMIT);
            return;
        }

        final int limFail = FactionsPlugin.getInstance().conf().factions().claims().getRadiusClaimFailureLimit();
        Tracker tracker = new Tracker();
        long x = 0;
        long z = 0;
        for (FLocation currentLocation : toClaim) {
            if (this.attemptUnclaim(sender, currentLocation, currentFaction, tracker)) {
                tracker.successes++;
                x += currentLocation.x();
                z += currentLocation.z();
            } else {
                tracker.fails++;
            }
            if (tracker.fails >= limFail) {
                sender.msg(TL.COMMAND_UNCLAIMFILL_TOOMUCHFAIL, tracker.fails);
                break;
            }
        }
        if (tracker.successes == 0) {
            sender.msg(TL.COMMAND_UNCLAIMFILL_BYPASSCOMPLETE, 0);
            return;
        }
        x = x / ((long) tracker.successes);
        z = z / ((long) tracker.successes);
        if (bypass) {
            sender.msg(TL.COMMAND_UNCLAIMFILL_BYPASSCOMPLETE, tracker.count());
        } else {
            if (tracker.refund != 0) {
                if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysLandCosts()) {
                    Econ.modifyMoney(forFaction, tracker.refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString());
                } else {
                    Econ.modifyMoney(sender, tracker.refund, TL.COMMAND_UNCLAIM_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIM_FORUNCLAIM.toString());
                }
            }
            currentFaction.msg(TL.COMMAND_UNCLAIMFILL_UNCLAIMED, sender.describeTo(currentFaction, true), tracker.count(), x + "," + z);
        }
    }

    private static class Tracker {
        private int successes;
        private int fails;
        private double refund;

        private int count() {
            return successes + fails;
        }
    }

    private void addIf(Set<FLocation> toClaim, Queue<FLocation> queue, FLocation examine, Faction replacement) {
        if (Board.board().factionAt(examine) == replacement && !toClaim.contains(examine)) {
            toClaim.add(examine);
            queue.add(examine);
        }
    }

    private boolean attemptUnclaim(FPlayer fPlayer, FLocation target, Faction targetFaction, Tracker tracker) {
        if (targetFaction.isSafeZone() || targetFaction.isWarZone()) {
            Board.board().unclaim(target);
            if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
                FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(fPlayer.name(), target.coordString(), targetFaction.tag()));
            }
            return true;
        }
        if (!fPlayer.adminBypass() && !targetFaction.hasAccess(fPlayer, PermissibleActions.TERRITORY, target)) {
            fPlayer.msg(TL.CLAIM_CANTUNCLAIM, targetFaction.describeTo(fPlayer));
            return false;
        }
        LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(target, targetFaction, fPlayer);
        Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
        if (unclaimEvent.isCancelled()) {
            return false;
        }

        if (!fPlayer.adminBypass() && Econ.shouldBeUsed()) {
            tracker.refund += Econ.calculateClaimRefund(targetFaction.claimCount());
        }

        Board.board().unclaim(target);

        if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIM_LOG.format(fPlayer.name(), target.coordString(), targetFaction.tag()));
        }
        return true;
    }

    private void unclaimAll(FPlayer sender, Faction faction, boolean confirmed) {
        if (sender.role() != Role.ADMIN && !sender.adminBypass()) {
            sender.msg(TL.CLAIM_CANTUNCLAIM, faction.describeTo(sender));
            return;
        }

        if (!confirmed) {
            String conf = CmdConfirm.add(sender, s -> this.unclaimAll(s, faction, true));
            // TODO TL
            sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to unclaim ALL " + faction.tag() + " territory? If so, run /f confirm " + conf);
            return;
        }

        LandUnclaimAllEvent unclaimAllEvent = new LandUnclaimAllEvent(faction, sender);
        Bukkit.getServer().getPluginManager().callEvent(unclaimAllEvent);
        if (unclaimAllEvent.isCancelled()) {
            return;
        }

        if (Econ.shouldBeUsed()) {
            double refund = Econ.calculateTotalLandRefund(faction.claimCount());
            if (FactionsPlugin.getInstance().conf().economy().isBankEnabled() && FactionsPlugin.getInstance().conf().economy().isBankFactionPaysLandCosts()) {
                if (!Econ.modifyMoney(faction, refund, TL.COMMAND_UNCLAIMALL_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIMALL_FORUNCLAIM.toString())) {
                    return;
                }
            } else {
                if (!Econ.modifyMoney(sender, refund, TL.COMMAND_UNCLAIMALL_TOUNCLAIM.toString(), TL.COMMAND_UNCLAIMALL_FORUNCLAIM.toString())) {
                    return;
                }
            }
        }

        Board.board().unclaimAll(faction);
        faction.msg(TL.COMMAND_UNCLAIMALL_UNCLAIMED, sender.describeTo(faction, true));

        if (FactionsPlugin.getInstance().conf().logging().isLandUnclaims()) {
            FactionsPlugin.getInstance().log(TL.COMMAND_UNCLAIMALL_LOG.format(sender.name(), faction.tag()));
        }
    }
}
