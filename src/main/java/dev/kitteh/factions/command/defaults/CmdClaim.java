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
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;
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

public class CmdClaim implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("claim")
                            .commandDescription(Cloudy.desc(TL.COMMAND_CLAIM_DESCRIPTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.CLAIM)
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
                                            .withComponent(IntegerParser.integerParser(1, FactionsPlugin.getInstance().conf().factions().claims().getFillClaimMaxClaims()))
                            )
                            .flag(
                                    manager.flagBuilder("auto")
                                            .withPermission(Cloudy.hasPermission(Permission.AUTOCLAIM))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        FLocation claimLocation = new FLocation(player);

        final Faction forFaction = context.flags().get("faction") instanceof Faction fac ? fac : sender.getFaction(); // Default to own

        if (context.flags().hasFlag("auto")) {
            if (sender.getAutoClaimFor() == null) {
                if (sender.canClaimForFaction(forFaction)) {
                    sender.setAutoClaimFor(forFaction);

                    sender.msg(TL.COMMAND_AUTOCLAIM_ENABLED, forFaction.describeTo(sender));
                    sender.attemptClaim(forFaction, claimLocation, true);
                } else {
                    sender.msg(TL.COMMAND_AUTOCLAIM_OTHERFACTION, forFaction.describeTo(sender));
                }
            } else {
                sender.setAutoClaimFor(null);
                sender.msg(TL.COMMAND_AUTOCLAIM_DISABLED);
            }

            return;
        }

        if (context.flags().hasFlag("fill")) {
            int limit = context.flags().get("fill-limit") instanceof Integer i ? i : FactionsPlugin.getInstance().conf().factions().claims().getFillClaimMaxClaims();

            this.fill(sender, claimLocation, forFaction, limit);

            return;
        }

        if (context.flags().get("radius") instanceof Integer radius) {
            if (!Permission.CLAIM_RADIUS.has(player)) {
                sender.msg(TL.COMMAND_CLAIM_DENIED);
                return;
            }

            new SpiralTask(new FLocation(player), radius) {
                private int failCount = 0;
                private final int limit = FactionsPlugin.getInstance().conf().factions().claims().getRadiusClaimFailureLimit() - 1;

                @Override
                public boolean work() {
                    boolean success = sender.attemptClaim(forFaction, this.currentLocation(), true);
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

        // Nothing else!
        sender.attemptClaim(forFaction, claimLocation, true);
    }

    private void fill(FPlayer sender, FLocation loc, Faction forFaction, int limit) {
        if (limit > FactionsPlugin.getInstance().conf().factions().claims().getFillClaimMaxClaims()) {
            sender.msg(TL.COMMAND_CLAIMFILL_ABOVEMAX, FactionsPlugin.getInstance().conf().factions().claims().getFillClaimMaxClaims());
            return;
        }

        final boolean bypass = sender.isAdminBypassing();

        Faction currentFaction = Board.getInstance().getFactionAt(loc);

        if (currentFaction.equals(forFaction)) {
            sender.msg(TL.CLAIM_ALREADYOWN, forFaction.describeTo(sender, true));
            return;
        }

        if (!bypass && !currentFaction.isWilderness()) {
            sender.msg(TL.COMMAND_CLAIMFILL_ALREADYCLAIMED);
            return;
        }

        if (!bypass &&
                (
                        (forFaction.isNormal() && !forFaction.hasAccess(sender, PermissibleActions.TERRITORY, null))
                                ||
                                (forFaction.isWarZone() && !Permission.MANAGE_WAR_ZONE.has(sender.getPlayer()))
                                ||
                                (forFaction.isSafeZone() && !Permission.MANAGE_SAFE_ZONE.has(sender.getPlayer()))
                )
        ) {
            sender.msg(TL.CLAIM_CANTCLAIM, forFaction.describeTo(sender));
            return;
        }

        final double distance = FactionsPlugin.getInstance().conf().factions().claims().getFillClaimMaxDistance();
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
                sender.msg(TL.COMMAND_CLAIMFILL_TOOFAR, distance);
                return;
            }

            addIf(toClaim, queue, currentHead.getRelative(0, 1), currentFaction);
            addIf(toClaim, queue, currentHead.getRelative(0, -1), currentFaction);
            addIf(toClaim, queue, currentHead.getRelative(1, 0), currentFaction);
            addIf(toClaim, queue, currentHead.getRelative(-1, 0), currentFaction);
        }

        if (toClaim.size() > limit) {
            sender.msg(TL.COMMAND_CLAIMFILL_PASTLIMIT);
            return;
        }

        if (forFaction.isNormal() && toClaim.size() > FactionsPlugin.getInstance().getLandRaidControl().getPossibleClaimCount(forFaction)) {
            sender.msg(TL.COMMAND_CLAIMFILL_NOTENOUGHLANDLEFT, forFaction.describeTo(sender), toClaim.size());
            return;
        }

        final int limFail = FactionsPlugin.getInstance().conf().factions().claims().getRadiusClaimFailureLimit();
        int fails = 0;
        for (FLocation currentLocation : toClaim) {
            if (!sender.attemptClaim(forFaction, currentLocation, true)) {
                fails++;
            }
            if (fails >= limFail) {
                sender.msg(TL.COMMAND_CLAIMFILL_TOOMUCHFAIL, fails);
                return;
            }
        }
    }

    private void addIf(Set<FLocation> toClaim, Queue<FLocation> queue, FLocation examine, Faction replacement) {
        if (Board.getInstance().getFactionAt(examine) == replacement && !toClaim.contains(examine)) {
            toClaim.add(examine);
            queue.add(examine);
        }
    }
}
