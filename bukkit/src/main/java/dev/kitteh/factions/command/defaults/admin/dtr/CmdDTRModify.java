package dev.kitteh.factions.command.defaults.admin.dtr;

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
import org.incendo.cloud.parser.standard.DoubleParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdDTRModify implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().dtr().modify();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MODIFY_DTR)))
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("amount", DoubleParser.doubleParser(0))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Faction target = context.get("faction");

        double amount = context.get("amount");
        if (amount == 0.0D) {
            return;
        }

        DTRControl dtr = (DTRControl) FactionsPlugin.instance().landRaidControl();
        target.dtr(Math.max(Math.min(target.dtr() + amount, dtr.getMaxDTR(target)), FactionsPlugin.instance().conf().factions().landRaidControl().dtr().getMinDTR()));
        context.sender().sendRichMessage(FactionsPlugin.instance().tl().commands().admin().dtr().modify().getSuccess(),
                FactionResolver.of(context.sender().fPlayerOrNull(), target));
    }
}
