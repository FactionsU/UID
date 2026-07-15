package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

public class CmdSetWarpProperty implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().set().warpProperty();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                            .required("name", StringParser.stringParser())
                            .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                            .flag(manager.flagBuilder("remove-password"))
                            .handler(this::handle)
            );
        };
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().warpProperty();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String warp = context.get("name");
        if (faction.warp(warp) == null) {
            sender.sendRichMessage(tl.getNoWarp(), Placeholder.unparsed("warp", warp));
            return;
        }

        if (context.flags().hasFlag("remove-password")) {
            faction.removeWarpPassword(warp);
            sender.sendRichMessage(tl.getRemovePassword(), Placeholder.unparsed("warp", warp));
        }

        String password = context.flags().get("password");

        if (password != null) {
            faction.setWarpPassword(warp, password);
            sender.sendRichMessage(tl.getSetPassword(), Placeholder.unparsed("warp", warp));
        }
    }
}
