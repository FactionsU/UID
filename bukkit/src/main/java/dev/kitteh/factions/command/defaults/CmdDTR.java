package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdDTR implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().dtr();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(
                                    Cloudy.predicate(_ -> FactionsPlugin.instance().landRaidControl() instanceof DTRControl)
                                            .and(Cloudy.hasPermission(Permission.DTR))
                            ))
                            .optional("faction", FactionParser.of(FactionParser.Include.PLAYERS))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().dtr();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        FPlayer fPlayer = context.sender().fPlayerOrNull();
        Faction faction = fPlayer != null && fPlayer.hasFaction() ? fPlayer.faction() : null;

        Faction target = context.getOrDefault("faction", faction);
        if (target == null) {
            return;
        }

        if (target != faction && !Permission.DTR_ANY.has(context.sender().sender())) {
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostDTR(), econTl.getDtrTo(), econTl.getDtrFor())) {
            return;
        }

        context.sender().sendRichMessage(tl.getDtr(), FactionResolver.of(target));
    }
}
