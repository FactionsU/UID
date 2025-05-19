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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CmdSet implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            List<String> aliases = new ArrayList<>(FactionsPlugin.instance().tl().commands().set().getAliases());
            Command.Builder<Sender> setBuilder = builder.literal(aliases.removeFirst(), aliases.toArray(new String[0])).permission(builder.commandPermission().and(Cloudy.hasFaction()));

            new CmdSetBoom().consumer().accept(manager, setBuilder);
            new CmdSetDefaultRole().consumer().accept(manager, setBuilder);
            new CmdSetDescription().consumer().accept(manager, setBuilder);
            new CmdSetHome().consumer().accept(manager, setBuilder);
            new CmdSetLink().consumer().accept(manager, setBuilder);
            new CmdSetOpen().consumer().accept(manager, setBuilder);
            new CmdSetPerm().consumer().accept(manager, setBuilder);
            new CmdSetTag().consumer().accept(manager, setBuilder);
            new CmdSetTitle().consumer().accept(manager, setBuilder);
            new CmdSetWarp().consumer().accept(manager, setBuilder);
            new CmdSetWarpProperty().consumer().accept(manager, setBuilder);
        };
    }
}
