package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.FlightUtil;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.WarmUpUtil;
import dev.kitteh.factions.util.ParticleProvider;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Particle;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.StringParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdFly implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().fly();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(
                                    Cloudy.predicate(_ -> FactionsPlugin.instance().conf().commands().fly().isEnable())
                                            .and(Cloudy.hasPermission(Permission.FLY))
                                            .and(Cloudy.hasFaction())))
                            .flag(manager.flagBuilder("auto").withPermission(Cloudy.hasPermission(Permission.FLY_AUTO)))
                            .flag(
                                    manager.flagBuilder("trail")
                                            .withPermission(Cloudy.hasPermission(Permission.FLY_TRAILS))
                                            .withComponent(BooleanParser.booleanParser(true))
                            )
                            .flag(
                                    manager.flagBuilder("particle")
                                            .withPermission(Cloudy.hasPermission(Permission.FLY_TRAILS))
                                            .withComponent(StringParser.stringParser())
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().fly();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean unhandled = true;
        if (context.flags().hasFlag("auto")) {
            sender.autoFlying(!sender.autoFlying());
            sender.sendRichMessage(tl.getAuto(), Placeholder.unparsed("state", sender.autoFlying() ? "enabled" : "disabled"));
            toggleFlight(sender, sender.autoFlying(), false);
            unhandled = false;
        }
        if (context.flags().get("trail") instanceof Boolean bool) {
            sender.flyTrail(bool);
            sender.sendRichMessage(tl.getTrailsChange(), Placeholder.unparsed("state", bool ? "enabled" : "disabled"));
            unhandled = false;
        }
        if (context.flags().get("particle") instanceof String effectName) {
            Particle particleEffect = ParticleProvider.effectFromString(effectName);
            if (particleEffect == null) {
                sender.sendRichMessage(tl.getTrailsParticleInvalid());
                return;
            }

            if (context.sender().sender().hasPermission(Permission.FLY_TRAILS + "." + effectName)) {
                sender.flyTrailEffect(effectName);
                sender.sendRichMessage(tl.getTrailsParticleChange(), Placeholder.unparsed("particle", effectName));
            } else {
                sender.sendRichMessage(tl.getTrailsParticlePerms(), Placeholder.unparsed("particle", effectName));
            }
            unhandled = false;
        }
        if (unhandled) {
            toggleFlight(sender, !sender.flying(), true);
        }
    }

    private void toggleFlight(FPlayer fPlayer, final boolean toggle, boolean notify) {
        if (!toggle) {
            fPlayer.flying(false);
            return;
        }
        if (!flyTest(fPlayer, notify)) {
            return;
        }

        var tl = FactionsPlugin.instance().tl().commands().fly();
        int delay = FactionsPlugin.instance().conf().commands().fly().getDelay();
        WarmUpUtil.process(fPlayer, WarmUpUtil.Warmup.FLIGHT,
                Mini.parse(tl.getWarmup(), fPlayer, Placeholder.unparsed("seconds", String.valueOf(delay))),
                () -> {
                    if (flyTest(fPlayer, notify)) {
                        fPlayer.flying(true);
                    }
                }, delay);
    }

    private boolean flyTest(FPlayer fPlayer, boolean notify) {
        var tl = FactionsPlugin.instance().tl().commands().fly();
        if (!fPlayer.canFlyAtLocation()) {
            if (notify) {
                fPlayer.sendRichMessage(tl.getNoAccess(), FactionResolver.of(fPlayer.lastStoodAt().faction()));
            }
            return false;
        } else if (FlightUtil.instance().enemiesNearby(fPlayer, FactionsPlugin.instance().conf().commands().fly().getEnemyRadius())) {
            if (notify) {
                fPlayer.sendRichMessage(tl.getEnemyNearby());
            }
            return false;
        }
        return true;
    }
}
