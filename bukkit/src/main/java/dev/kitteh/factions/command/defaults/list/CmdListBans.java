package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdListBans implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().list().bans();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasFaction())))
                            .optional("faction", FactionParser.of(FactionParser.Include.SELF))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction target = context.getOrDefault("faction", sender.faction());

        if (!target.isNormal()) {
            sender.msgLegacy(TL.COMMAND_BANLIST_NOFACTION);
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(TL.COMMAND_BANLIST_HEADER.format(target.bans().size(), target.tagLegacy(sender)));
        int i = 1;

        for (BanInfo info : target.bans()) {
            FPlayer banned = FPlayers.fPlayers().get(info.banned());
            FPlayer banner = FPlayers.fPlayers().get(info.banner());
            String timestamp = TL.sdf.format(info.time());

            lines.add(TL.COMMAND_BANLIST_ENTRY.format(i, banned.name(), banner.name(), timestamp));
            i++;
        }

        for (String s : lines) {
            sender.sendMessageLegacy(s);
        }
    }
}
