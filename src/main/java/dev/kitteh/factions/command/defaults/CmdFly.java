package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.FlightUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.WarmUpUtil;
import dev.kitteh.factions.util.ParticleProvider;
import org.bukkit.Particle;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.BooleanParser;

import java.util.function.BiConsumer;

public class CmdFly implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("fly")
                        .commandDescription(Cloudy.desc(TL.COMMAND_FLY_DESCRIPTION))
                        .permission(builder.commandPermission().and(
                                Cloudy.predicate(s -> FactionsPlugin.instance().conf().commands().fly().isEnable())
                                        .and(Cloudy.hasPermission(Permission.FLY))
                                        .and(Cloudy.hasFaction())))
                        .flag(manager.flagBuilder("auto").withPermission(Cloudy.hasPermission(Permission.FLY_AUTO)))
                        .flag(
                                manager.flagBuilder("trail")
                                        .withPermission(Cloudy.hasPermission(Permission.FLY_TRAILS))
                                        .withComponent(BooleanParser.booleanParser(true))
                        )
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean unhandled = true;
        if (context.flags().hasFlag("auto")) {
            //if (Permission.FLY_AUTO.has(context.sender().sender(), true)) {
            sender.autoFlying(!sender.autoFlying());
            toggleFlight(sender, sender.autoFlying(), false);
            unhandled = false;
            //}
        }
        if (context.flags().get("trail") instanceof Boolean bool) {
            sender.flyTrail(bool);
            unhandled = false;
        }
        if (context.flags().get("particle") instanceof String effectName) {
            Particle particleEffect = ParticleProvider.effectFromString(effectName);
            if (particleEffect == null) {
                sender.msg(TL.COMMAND_FLYTRAILS_PARTICLE_INVALID);
                return;
            }

            if (context.sender().sender().hasPermission(Permission.FLY_TRAILS.node + "." + effectName)) {
                sender.flyTrailEffect(effectName);
            } else {
                sender.msg(TL.COMMAND_FLYTRAILS_PARTICLE_PERMS, effectName);
            }
            unhandled = false;
        }
        if (unhandled) {
            toggleFlight(sender, !sender.flying(), true);
        }
    }

    private void toggleFlight(FPlayer fPlayer, final boolean toggle, boolean notify) {
        // If false do nothing besides set
        if (!toggle) {
            fPlayer.flying(false);
            return;
        }
        // Do checks if true
        if (!flyTest(fPlayer, notify)) {
            return;
        }

        WarmUpUtil.process(fPlayer, WarmUpUtil.Warmup.FLIGHT, TL.WARMUPS_NOTIFY_FLIGHT, "Fly", () -> {
            if (flyTest(fPlayer, notify)) {
                fPlayer.flying(true);
            }
        }, FactionsPlugin.instance().conf().commands().fly().getDelay());
    }

    private boolean flyTest(FPlayer fPlayer, boolean notify) {
        if (!fPlayer.canFlyAtLocation()) {
            if (notify) {
                Faction factionAtLocation = Board.board().factionAt(fPlayer.lastStoodAt());
                fPlayer.msg(TL.COMMAND_FLY_NO_ACCESS, factionAtLocation.tagLegacy(fPlayer));
            }
            return false;
        } else if (FlightUtil.instance().enemiesNearby(fPlayer, FactionsPlugin.instance().conf().commands().fly().getEnemyRadius())) {
            if (notify) {
                fPlayer.msg(TL.COMMAND_FLY_ENEMY_NEARBY);
            }
            return false;
        }
        return true;
    }
}
