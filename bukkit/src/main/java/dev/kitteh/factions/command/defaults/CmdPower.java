package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdPower implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().power();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(
                                    Cloudy.predicate(s -> FactionsPlugin.instance().landRaidControl() instanceof PowerControl)
                                            .and(Cloudy.hasPermission(Permission.POWER))
                            ))
                            .optional("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().power();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer sender = context.sender().fPlayerOrNull();

        FPlayer target = context.getOrDefault("player", sender);
        if (target == null) {
            return;
        }

        if (target != sender && !Permission.POWER_ANY.has(context.sender().sender())) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostPower(), econTl.getPowerTo(), econTl.getPowerFor())) {
            return;
        }

        double powerBoost = target.powerBoost();
        String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? tl.getBonus() : tl.getPenalty()) + powerBoost + ")";
        context.sender().sendRichMessage(tl.getPower(),
                FPlayerResolver.of("player", target),
                Placeholder.unparsed("power", String.valueOf(target.powerRounded())),
                Placeholder.unparsed("power_max", String.valueOf(target.powerMaxRounded())),
                Placeholder.unparsed("bonus_penalty", boost));
    }
}
