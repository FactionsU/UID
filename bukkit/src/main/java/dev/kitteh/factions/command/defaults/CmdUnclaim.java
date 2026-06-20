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
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdUnclaim implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().unclaim();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
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
                                            .withComponent(IntegerParser.integerParser(1, FactionsPlugin.instance().conf().factions().claims().getFillUnClaimMaxClaims()))
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
        var tl = FactionsPlugin.instance().tl().commands().unclaim();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        FLocation claimLocation = new FLocation(player);

        final Faction forFaction = context.flags().get("faction") instanceof Faction fac ? fac : sender.faction();

        if (context.flags().hasFlag("all-territory")) {
            unclaimAll(sender, forFaction, false);
            return;
        }

        if (context.flags().hasFlag("auto")) {
            if (sender.autoUnclaim() == null) {
                if (sender.canClaimForFaction(forFaction)) {
                    sender.autoUnclaim(forFaction);
                    sender.sendRichMessage(tl.getAutoUnclaimEnabled(), Placeholder.unparsed("faction", forFaction.tag()));
                    sender.attemptUnclaim(forFaction, claimLocation, true);
                } else {
                    sender.sendRichMessage(tl.getAutoUnclaimOtherFaction(), Placeholder.unparsed("faction", forFaction.tag()));
                }
            } else {
                sender.autoUnclaim(null);
                sender.sendRichMessage(tl.getAutoUnclaimDisabled());
            }
            return;
        }

        if (context.flags().hasFlag("fill")) {
            int limit = context.flags().get("fill-limit") instanceof Integer i ? i : FactionsPlugin.instance().conf().factions().claims().getFillUnClaimMaxClaims();
            this.fill(sender, claimLocation, forFaction, limit);
            return;
        }

        if (context.flags().get("radius") instanceof Integer radius && radius > 1) {
            if (!Permission.CLAIM_RADIUS.has(player)) {
                sender.sendRichMessage(tl.getCantUnclaim(), Placeholder.unparsed("faction", forFaction.tag()));
                return;
            }

            new SpiralTask(claimLocation, radius) {
                private int failCount = 0;
                private final int limit = FactionsPlugin.instance().conf().factions().claims().getRadiusClaimFailureLimit() - 1;

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
        var tl = FactionsPlugin.instance().tl().commands().unclaim();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (limit > FactionsPlugin.instance().conf().factions().claims().getFillUnClaimMaxClaims()) {
            sender.sendRichMessage(tl.getFillAboveMax(), Placeholder.unparsed("max", String.valueOf(FactionsPlugin.instance().conf().factions().claims().getFillUnClaimMaxClaims())));
            return;
        }

        final boolean bypass = sender.adminBypass();

        Faction currentFaction = Board.board().factionAt(loc);

        if (currentFaction != forFaction) {
            sender.sendRichMessage(tl.getFillNotClaimed());
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
            sender.sendRichMessage(tl.getCantUnclaim(), Placeholder.unparsed("faction", forFaction.tag()));
            return;
        }

        final double distance = FactionsPlugin.instance().conf().factions().claims().getFillUnClaimMaxDistance();
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
                sender.sendRichMessage(tl.getFillTooFar(), Placeholder.unparsed("max", String.valueOf(distance)));
                return;
            }

            addIf(toClaim, queue, currentHead.relative(0, 1), currentFaction);
            addIf(toClaim, queue, currentHead.relative(0, -1), currentFaction);
            addIf(toClaim, queue, currentHead.relative(1, 0), currentFaction);
            addIf(toClaim, queue, currentHead.relative(-1, 0), currentFaction);
        }

        if (toClaim.size() > limit) {
            sender.sendRichMessage(tl.getFillPastLimit());
            return;
        }

        final int limFail = FactionsPlugin.instance().conf().factions().claims().getRadiusClaimFailureLimit();
        Tracker tracker = new Tracker();
        long x = 0;
        long z = 0;
        for (FLocation currentLocation : toClaim) {
            if (this.attemptUnclaimForFill(sender, currentLocation, currentFaction, tracker)) {
                tracker.successes++;
                x += currentLocation.x();
                z += currentLocation.z();
            } else {
                tracker.fails++;
            }
            if (tracker.fails >= limFail) {
                sender.sendRichMessage(tl.getFillTooMuchFail(), Placeholder.unparsed("count", String.valueOf(tracker.fails)));
                break;
            }
        }
        if (tracker.successes == 0) {
            sender.sendRichMessage(tl.getFillBypassComplete(), Placeholder.unparsed("count", "0"));
            return;
        }
        x = x / ((long) tracker.successes);
        z = z / ((long) tracker.successes);
        if (bypass) {
            sender.sendRichMessage(tl.getFillBypassComplete(), Placeholder.unparsed("count", String.valueOf(tracker.count())));
        } else {
            if (tracker.refund != 0) {
                if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysLandCosts()) {
                    Econ.modifyMoney(forFaction, tracker.refund, econTl.getUnclaimTo(), econTl.getUnclaimFor());
                } else {
                    Econ.modifyMoney(sender, tracker.refund, econTl.getUnclaimTo(), econTl.getUnclaimFor());
                }
            }
            currentFaction.sendRichMessage(tl.getFillUnclaimed(),
                    FPlayerResolver.of("player", sender),
                    Placeholder.unparsed("count", String.valueOf(tracker.count())),
                    Placeholder.unparsed("location", x + "," + z));
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

    private boolean attemptUnclaimForFill(FPlayer fPlayer, FLocation target, Faction targetFaction, Tracker tracker) {
        var tl = FactionsPlugin.instance().tl().commands().unclaim();
        if (targetFaction.isSafeZone() || targetFaction.isWarZone()) {
            Board.board().unclaim(target);
            if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
                AbstractFactionsPlugin.instance().log(fPlayer.name() + " unclaimed " + target.asCoordString() + " from " + targetFaction.tag());
            }
            return true;
        }
        if (!fPlayer.adminBypass() && !targetFaction.hasAccess(fPlayer, PermissibleActions.TERRITORY, target)) {
            fPlayer.sendRichMessage(tl.getCantUnclaim(), Placeholder.unparsed("faction", targetFaction.tag()));
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

        if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
            AbstractFactionsPlugin.instance().log(fPlayer.name() + " unclaimed " + target.asCoordString() + " from " + targetFaction.tag());
        }
        return true;
    }

    public static void unclaimAll(FPlayer sender, Faction faction, boolean confirmed) {
        var tl = FactionsPlugin.instance().tl().commands().unclaim();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        if (sender.role() != Role.ADMIN && !sender.adminBypass()) {
            sender.sendRichMessage(tl.getCantUnclaim(), FactionResolver.of(faction));
            return;
        }

        if (!confirmed) {
            String conf = CmdConfirm.add(sender, s -> unclaimAll(s, faction, true));
            sender.sendRichMessage(tl.getUnclaimAllConfirm(),
                    FactionResolver.of(faction),
                    Placeholder.unparsed("command", conf));
            return;
        }

        LandUnclaimAllEvent unclaimAllEvent = new LandUnclaimAllEvent(faction, sender);
        Bukkit.getServer().getPluginManager().callEvent(unclaimAllEvent);
        if (unclaimAllEvent.isCancelled()) {
            return;
        }

        if (Econ.shouldBeUsed()) {
            double refund = Econ.calculateTotalLandRefund(faction.claimCount());
            if (FactionsPlugin.instance().conf().economy().isBankEnabled() && FactionsPlugin.instance().conf().economy().isBankFactionPaysLandCosts()) {
                if (!Econ.modifyMoney(faction, refund, econTl.getUnclaimAllTo(), econTl.getUnclaimAllFor())) {
                    return;
                }
            } else {
                if (!Econ.modifyMoney(sender, refund, econTl.getUnclaimAllTo(), econTl.getUnclaimAllFor())) {
                    return;
                }
            }
        }

        Board.board().unclaimAll(faction);
        faction.sendRichMessage(tl.getUnclaimAllUnclaimed(), FPlayerResolver.of("player", sender));

        if (FactionsPlugin.instance().conf().logging().isLandUnclaims()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " unclaimed all of " + faction.tag());
        }
    }
}
