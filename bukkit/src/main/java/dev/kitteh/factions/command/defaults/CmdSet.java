package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.set.CmdSetBoom;
import dev.kitteh.factions.command.defaults.set.CmdSetDefaultRole;
import dev.kitteh.factions.command.defaults.set.CmdSetDescription;
import dev.kitteh.factions.command.defaults.set.CmdSetHome;
import dev.kitteh.factions.command.defaults.set.CmdSetLink;
import dev.kitteh.factions.command.defaults.set.CmdSetOpen;
import dev.kitteh.factions.command.defaults.set.CmdSetPerm;
import dev.kitteh.factions.command.defaults.set.CmdSetTag;
import dev.kitteh.factions.command.defaults.set.CmdSetTitle;
import dev.kitteh.factions.command.defaults.set.CmdSetWarp;
import dev.kitteh.factions.command.defaults.set.CmdSetWarpProperty;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSet implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var setConf = FactionsPlugin.instance().tl().commands().set();
            Command.Builder<Sender> setBuilder = builder.literal(setConf.getFirstAlias(), setConf.getSecondaryAliases()).permission(builder.commandPermission().and(Cloudy.hasFaction()));

            new CmdSetBoom().consumer().accept(manager, setBuilder, help);
            new CmdSetDefaultRole().consumer().accept(manager, setBuilder, help);
            new CmdSetDescription().consumer().accept(manager, setBuilder, help);
            new CmdSetHome().consumer().accept(manager, setBuilder, help);
            new CmdSetLink().consumer().accept(manager, setBuilder, help);
            new CmdSetOpen().consumer().accept(manager, setBuilder, help);
            new CmdSetPerm().consumer().accept(manager, setBuilder, help);
            new CmdSetTag().consumer().accept(manager, setBuilder, help);
            new CmdSetTitle().consumer().accept(manager, setBuilder, help);
            new CmdSetWarp().consumer().accept(manager, setBuilder, help);
            new CmdSetWarpProperty().consumer().accept(manager, setBuilder, help);
        };
    }
}
