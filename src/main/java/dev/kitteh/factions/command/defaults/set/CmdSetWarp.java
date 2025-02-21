package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.LazyLocation;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.function.BiConsumer;

public class CmdSetWarp implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("warp")
                            .commandDescription(Cloudy.desc(TL.COMMAND_SETFWARP_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                            .required("name", StringParser.stringParser())
                            // TODO more flags, translatable password string
                            .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                            .flag(manager.flagBuilder("delete"))
                            .handler(this::handle)
            );
        };
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        Player player = ((Sender.Player) context.sender()).player();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.getFaction();

        String warp = context.get("name");

        if (context.flags().hasFlag("delete")) {
            if (faction.isWarp(warp)) {
                if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDelWarp(), TL.COMMAND_DELFWARP_TODELETE, TL.COMMAND_DELFWARP_FORDELETE)) {
                    return;
                }
                faction.removeWarp(warp);
                sender.msg(TL.COMMAND_DELFWARP_DELETED, warp);
            } else {
                sender.msg(TL.COMMAND_DELFWARP_INVALID, warp);
            }
            return;
        }

        if (Board.getInstance().getFactionAt(new FLocation(player)) != faction) {
            sender.msg(TL.COMMAND_SETFWARP_NOTCLAIMED);
            return;
        }

        int maxWarps = FactionsPlugin.getInstance().conf().commands().warp().getMaxWarps();
        if (maxWarps <= faction.getWarps().size()) {
            sender.msg(TL.COMMAND_SETFWARP_LIMIT, maxWarps);
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().homes().isRequiredToHaveHomeBeforeSettingWarps() && !faction.hasHome()) {
            sender.msg(TL.COMMAND_SETFWARP_HOMEREQUIRED);
        }

        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostSetWarp(), TL.COMMAND_SETFWARP_TOSET, TL.COMMAND_SETFWARP_FORSET)) {
            return;
        }

        String password = context.flags().get("password");

        LazyLocation loc = new LazyLocation(player.getLocation());
        faction.setWarp(warp, loc);
        if (password != null) {
            faction.setWarpPassword(warp, password);
        }
        sender.msg(TL.COMMAND_SETFWARP_SET, warp, password != null ? password : "");
    }
}
