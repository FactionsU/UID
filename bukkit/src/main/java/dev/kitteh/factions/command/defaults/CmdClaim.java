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
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SpiralTask;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

public class CmdClaim implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> manager.command(
                builder.literal("claim")
                        .commandDescription(Cloudy.desc(FactionsPlugin.instance().tl().commands().claim().getDescription()))
                        .permission(
                                builder.commandPermission()
                                        .and(Cloudy.hasPermission(Permission.CLAIM)
                                                .and(Cloudy.isPlayer())
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
                                        .withPermission(Cloudy.hasPermission(Permission.CLAIM_FILL))
                        )
                        .flag(
                                manager.flagBuilder("fill-limit")
                                        .withPermission(Cloudy.hasPermission(Permission.CLAIM_FILL))
                                        .withComponent(IntegerParser.integerParser(1, FactionsPlugin.instance().conf().factions().claims().getFillClaimMaxClaims()))
                        )
                        .flag(
                                manager.flagBuilder("auto")
                                        .withPermission(Cloudy.hasPermission(Permission.AUTOCLAIM))
                        )
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().claim();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        FLocation claimLocation = new FLocation(player);

        final Faction forFaction = context.flags().get("faction") instanceof Faction fac ? fac : sender.faction(); // Default to own

        if (context.flags().hasFlag("auto")) {
            if (sender.autoClaim() == null) {
                if (sender.canClaimForFaction(forFaction)) {
                    sender.autoClaim(forFaction);
                    sender.sendRichMessage(tl.getAutoClaimEnabled(), FactionResolver.of(forFaction));
                    sender.attemptClaim(forFaction, claimLocation, true);
                } else {
                    sender.sendRichMessage(tl.getAutoClaimOtherFaction(), FactionResolver.of(forFaction));
                }
            } else {
                sender.autoClaim(null);
                sender.sendRichMessage(tl.getAutoClaimDisabled());
            }

            return;
        }

        if (context.flags().hasFlag("fill")) {
            int limit = context.flags().get("fill-limit") instanceof Integer i ? i : FactionsPlugin.instance().conf().factions().claims().getFillClaimMaxClaims();
            this.fill(sender, claimLocation, forFaction, limit);
            return;
        }

        if (context.flags().get("radius") instanceof Integer radius && radius > 1) {
            if (!Permission.CLAIM_RADIUS.has(player)) {
                sender.sendRichMessage(tl.getClaimDenied());
                return;
            }

            new SpiralTask(claimLocation, radius) {
                private int failCount = 0;
                private final int limit = FactionsPlugin.instance().conf().factions().claims().getRadiusClaimFailureLimit() - 1;

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

        sender.attemptClaim(forFaction, claimLocation, true);
    }

    private void fill(FPlayer sender, FLocation loc, Faction forFaction, int limit) {
        var tl = FactionsPlugin.instance().tl().commands().claim();
        if (limit > FactionsPlugin.instance().conf().factions().claims().getFillClaimMaxClaims()) {
            sender.sendRichMessage(tl.getFillAboveMax(), Placeholder.unparsed("max", String.valueOf(FactionsPlugin.instance().conf().factions().claims().getFillClaimMaxClaims())));
            return;
        }

        final boolean bypass = sender.adminBypass();

        Faction currentFaction = Board.board().factionAt(loc);

        if (currentFaction.equals(forFaction)) {
            sender.sendRichMessage(tl.getAlreadyOwn(), FactionResolver.of(forFaction));
            return;
        }

        if (!bypass && !currentFaction.isWilderness()) {
            sender.sendRichMessage(tl.getFillAlreadyClaimed());
            return;
        }

        if (!bypass &&
                (
                        (forFaction.isNormal() && !forFaction.hasAccess(sender, PermissibleActions.TERRITORY, null))
                                ||
                                (forFaction.isWarZone() && !Permission.MANAGE_WAR_ZONE.has(sender.asPlayer()))
                                ||
                                (forFaction.isSafeZone() && !Permission.MANAGE_SAFE_ZONE.has(sender.asPlayer()))
                )
        ) {
            sender.sendRichMessage(tl.getCantClaim(), FactionResolver.of(forFaction));
            return;
        }

        final double distance = FactionsPlugin.instance().conf().factions().claims().getFillClaimMaxDistance();
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

        if (forFaction.isNormal() && toClaim.size() > FactionsPlugin.instance().landRaidControl().possibleClaimCount(forFaction)) {
            sender.sendRichMessage(tl.getFillNotEnoughLand(),
                    Placeholder.unparsed("faction", forFaction.tag()),
                    Placeholder.unparsed("count", String.valueOf(toClaim.size())));
            return;
        }

        final int limFail = FactionsPlugin.instance().conf().factions().claims().getRadiusClaimFailureLimit();
        int fails = 0;
        for (FLocation currentLocation : toClaim) {
            if (!sender.attemptClaim(forFaction, currentLocation, true)) {
                fails++;
            }
            if (fails >= limFail) {
                sender.sendRichMessage(tl.getFillTooMuchFail(), Placeholder.unparsed("count", String.valueOf(fails)));
                return;
            }
        }
    }

    private void addIf(Set<FLocation> toClaim, Queue<FLocation> queue, FLocation examine, Faction replacement) {
        if (Board.board().factionAt(examine) == replacement && !toClaim.contains(examine)) {
            toClaim.add(examine);
            queue.add(examine);
        }
    }
}
