package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
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
import java.util.function.BiConsumer;

public class CmdListBans implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("banlist")
                            .commandDescription(Cloudy.desc(TL.COMMAND_BANLIST_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasFaction())))
                            .optional("faction", FactionParser.of(FactionParser.Include.SELF))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction target = context.getOrDefault("faction", sender.getFaction());

        if (!target.isNormal()) {
            sender.msg(TL.COMMAND_BANLIST_NOFACTION);
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(TL.COMMAND_BANLIST_HEADER.format(target.getBannedPlayers().size(), target.getTag(sender)));
        int i = 1;

        for (BanInfo info : target.getBannedPlayers()) {
            FPlayer banned = FPlayers.getInstance().getById(info.banned());
            FPlayer banner = FPlayers.getInstance().getById(info.banner());
            String timestamp = TL.sdf.format(info.time());

            lines.add(TL.COMMAND_BANLIST_ENTRY.format(i, banned.getName(), banner.getName(), timestamp));
            i++;
        }

        for (String s : lines) {
            sender.sendMessage(s);
        }
    }
}
