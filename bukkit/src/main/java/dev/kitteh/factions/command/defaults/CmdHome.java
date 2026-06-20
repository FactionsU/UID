package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerTeleportEvent;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.SmokeUtil;
import dev.kitteh.factions.util.WarmUpUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdHome implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().home();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(
                                    Cloudy.hasPermission(Permission.HOME)
                                            .and(Cloudy.hasFaction())
                                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().homes().isEnabled()))
                                            .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().homes().isTeleportCommandEnabled()))
                            ))
                            .flag(
                                    manager.flagBuilder("faction")
                                            .withComponent(FactionParser.of())
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().home();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        Faction targetFaction = sender.faction();

        if (context.flags().get("faction") instanceof Faction fac) {
            targetFaction = fac;
        }

        if (sender.adminBypass()) {
            AbstractFactionsPlugin.instance().teleport(player, targetFaction.home());
            return;
        }

        if (!targetFaction.hasAccess(sender, PermissibleActions.HOME, sender.lastStoodAt())) {
            sender.sendRichMessage(tl.getDenied(), FactionResolver.of(targetFaction));
            return;
        }

        if (!(targetFaction.home() instanceof Location destination)) {
            sender.sendRichMessage(tl.getNoHome());
            return;
        }

        if (!FactionsPlugin.instance().conf().factions().homes().isTeleportAllowedFromEnemyTerritory() && sender.isInEnemyTerritory()) {
            sender.sendRichMessage(tl.getInEnemy());
            return;
        }

        if (!FactionsPlugin.instance().conf().factions().homes().isTeleportAllowedFromDifferentWorld() && !player.getWorld().equals(destination.getWorld())) {
            sender.sendRichMessage(tl.getWrongWorld());
            return;
        }

        Faction faction = Board.board().factionAt(new FLocation(player.getLocation()));
        final Location loc = player.getLocation().clone();

        if (FactionsPlugin.instance().conf().factions().homes().getTeleportAllowedEnemyDistance() > 0 &&
                !faction.isSafeZone() &&
                (!sender.isInOwnTerritory() || !FactionsPlugin.instance().conf().factions().homes().isTeleportIgnoreEnemiesIfInOwnTerritory())) {
            World w = loc.getWorld();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            for (Player p : player.getServer().getOnlinePlayers()) {
                if (p == null || !p.isOnline() || p.isDead() || p.getUniqueId().equals(player.getUniqueId()) || p.getWorld() != w) {
                    continue;
                }

                FPlayer fp = FPlayers.fPlayers().get(p);
                if (sender.relationTo(fp) != Relation.ENEMY || fp.isVanished()) {
                    continue;
                }

                Location l = p.getLocation();
                double dx = Math.abs(x - l.getX());
                double dy = Math.abs(y - l.getY());
                double dz = Math.abs(z - l.getZ());
                double max = FactionsPlugin.instance().conf().factions().homes().getTeleportAllowedEnemyDistance();

                if (dx > max || dy > max || dz > max) {
                    continue;
                }

                sender.sendRichMessage(tl.getEnemyNear(), Placeholder.unparsed("range", String.valueOf(FactionsPlugin.instance().conf().factions().homes().getTeleportAllowedEnemyDistance())));
                return;
            }
        }

        FPlayerTeleportEvent tpEvent = new FPlayerTeleportEvent(sender, destination, FPlayerTeleportEvent.Reason.HOME);
        Bukkit.getServer().getPluginManager().callEvent(tpEvent);
        if (tpEvent.isCancelled()) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostHome(), econTl.getHomeTo(), econTl.getHomeFor())) {
            return;
        }

        int delay = FactionsPlugin.instance().conf().commands().home().getDelay();
        WarmUpUtil.process(sender, WarmUpUtil.Warmup.HOME,
                Mini.parse(tl.getWarmup(), sender, Placeholder.unparsed("seconds", String.valueOf(delay))),
                () -> {
                    Player plr = sender.asPlayer();

                    if (plr == null) return;

                    if (ExternalChecks.tryTeleport(plr, destination)) {
                        return;
                    }

                    if (FactionsPlugin.instance().conf().factions().homes().isTeleportCommandSmokeEffectEnabled()) {
                        List<Location> smokeLocations = new ArrayList<>();
                        smokeLocations.add(loc);
                        smokeLocations.add(loc.add(0, 1, 0));
                        smokeLocations.add(destination);
                        smokeLocations.add(destination.clone().add(0, 1, 0));
                        SmokeUtil.spawnCloudRandom(smokeLocations, FactionsPlugin.instance().conf().factions().homes().getTeleportCommandSmokeEffectThickness());
                    }

                    AbstractFactionsPlugin.instance().teleport(plr, destination);
                }, delay);
    }
}
