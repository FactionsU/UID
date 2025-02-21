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

import java.util.function.BiConsumer;

public class CmdAdminPower implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> powerBuilder = builder.literal("power")
                    .permission(builder.commandPermission().and(Cloudy.predicate(s-> FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl)));

            new CmdPowerBoost().consumer().accept(manager, powerBuilder);
            new CmdModifyPower().consumer().accept(manager, powerBuilder);
            new CmdPermanentPower().consumer().accept(manager, powerBuilder);
            new CmdSetPower().consumer().accept(manager, powerBuilder);
        };
    }
}
