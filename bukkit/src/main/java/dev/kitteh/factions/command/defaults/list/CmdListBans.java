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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.time.Instant;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdListBans implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
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
        var tl = FactionsPlugin.instance().tl().commands().list().bans();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction target = context.getOrDefault("faction", sender.faction());

        if (!target.isNormal()) {
            sender.sendRichMessage(tl.getNoFaction());
            return;
        }

        sender.sendRichMessage(tl.getHeader(),
                Placeholder.unparsed("count", String.valueOf(target.bans().size())),
                Placeholder.unparsed("faction", target.tag()));

        var date = FactionsPlugin.instance().tl().placeholders().datesAndTimes();

        int i = 1;
        for (BanInfo info : target.bans()) {
            FPlayer banned = FPlayers.fPlayers().get(info.banned());
            FPlayer banner = FPlayers.fPlayers().get(info.banner());
            sender.sendRichMessage(tl.getEntry(),
                    Placeholder.unparsed("index", String.valueOf(i)),
                    Placeholder.unparsed("player", banned.name()),
                    Placeholder.unparsed("banner", banner.name()),
                    Placeholder.unparsed("date", date.formatBanTiming(Instant.ofEpochMilli(info.time()))));
            i++;
        }
    }
}
