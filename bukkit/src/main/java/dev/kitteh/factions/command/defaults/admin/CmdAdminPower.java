package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.power.CmdModifyPower;
import dev.kitteh.factions.command.defaults.admin.power.CmdPermanentPower;
import dev.kitteh.factions.command.defaults.admin.power.CmdPowerBoost;
import dev.kitteh.factions.command.defaults.admin.power.CmdSetPower;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAdminPower implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().power();
            Command.Builder<Sender> powerBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> FactionsPlugin.instance().landRaidControl() instanceof PowerControl)));

            new CmdPowerBoost().consumer().accept(manager, powerBuilder, help);
            new CmdModifyPower().consumer().accept(manager, powerBuilder, help);
            new CmdPermanentPower().consumer().accept(manager, powerBuilder, help);
            new CmdSetPower().consumer().accept(manager, powerBuilder, help);
        };
    }
}
