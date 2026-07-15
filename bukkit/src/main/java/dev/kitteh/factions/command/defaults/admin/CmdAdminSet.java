package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetAutoSave;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetBoom;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetGrace;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetPeaceful;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetPermanent;
import dev.kitteh.factions.command.defaults.admin.set.CmdSetTag;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAdminSet implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = Confs.tl().commands().admin().set();
            Command.Builder<Sender> setBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases());

            new CmdSetAutoSave().consumer().accept(manager, setBuilder, help);
            new CmdSetBoom().consumer().accept(manager, setBuilder, help);
            new CmdSetGrace().consumer().accept(manager, setBuilder, help);
            new CmdSetPeaceful().consumer().accept(manager, setBuilder, help);
            new CmdSetPermanent().consumer().accept(manager, setBuilder, help);
            new CmdSetTag().consumer().accept(manager, setBuilder, help);
        };
    }
}
