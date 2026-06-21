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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetWarp implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().set().warp();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SETWARP).and(Cloudy.hasSelfFactionPerms(PermissibleActions.SETWARP))))
                            .required("name", StringParser.stringParser())
                            .flag(manager.flagBuilder("password").withComponent(StringParser.stringParser()))
                            .flag(manager.flagBuilder("delete"))
                            .handler(this::handle)
            );
        };
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().set().warp();
        var econTl = FactionsPlugin.instance().tl().economy().actions();
        Player player = ((Sender.Player) context.sender()).player();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String warp = context.get("name");

        if (context.flags().hasFlag("delete")) {
            if (faction.isWarp(warp)) {
                if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostDelWarp(), econTl.getDelWarpTo(), econTl.getDelWarpFor())) {
                    return;
                }
                faction.removeWarp(warp);
                sender.sendRichMessage(tl.getDeleted(), Placeholder.unparsed("warp", warp));
            } else {
                sender.sendRichMessage(tl.getDeleteNotFound(), Placeholder.unparsed("warp", warp));
            }
            return;
        }

        if (Board.board().factionAt(new FLocation(player)) != faction) {
            sender.sendRichMessage(tl.getNotClaimed());
            return;
        }

        int maxWarps = faction.maxWarps();
        if (maxWarps <= faction.warps().size()) {
            sender.sendRichMessage(tl.getLimit(), Placeholder.unparsed("max", String.valueOf(maxWarps)));
            return;
        }

        if (FactionsPlugin.instance().conf().factions().homes().isRequiredToHaveHomeBeforeSettingWarps() && !faction.hasHome()) {
            sender.sendRichMessage(tl.getHomeRequired());
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostSetWarp(), econTl.getSetWarpTo(), econTl.getSetWarpFor())) {
            return;
        }

        String password = context.flags().get("password");

        LazyLocation loc = new LazyLocation(player.getLocation());
        faction.createWarp(warp, loc);
        if (password != null) {
            faction.setWarpPassword(warp, password);
        }
        sender.sendRichMessage(tl.getSet(),
                Placeholder.unparsed("warp", warp),
                Placeholder.unparsed("password", password != null ? password : ""));
    }
}
