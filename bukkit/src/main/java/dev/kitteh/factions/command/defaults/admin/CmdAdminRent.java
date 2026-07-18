package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CmdAdminRent implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().admin().rent();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.predicate(_ -> Econ.rentEnabled())).and(Cloudy.hasPermission(Permission.RENT)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().admin().rent();

        List<Faction> normal = Factions.factions().all().stream().filter(Faction::isNormal).toList();

        List<Faction> debtors = normal.stream()
                .filter(faction -> faction.rentDebt() > 0)
                .sorted(Comparator.<Faction>comparingDouble(Faction::rentDebt).reversed())
                .toList();

        List<Missed> missed = new ArrayList<>();
        for (Faction faction : normal) {
            List<LocalDate> dates = faction.missedRentDates();
            if (!dates.isEmpty()) {
                missed.add(new Missed(faction, dates.size(), Collections.max(dates)));
            }
        }
        missed.sort(Comparator.comparing(Missed::mostRecent).reversed());

        if (debtors.isEmpty() && missed.isEmpty()) {
            context.sender().sendRichMessage(tl.getNone());
            return;
        }

        if (!debtors.isEmpty()) {
            context.sender().sendRichMessage(tl.getHeader());
            for (Faction faction : debtors) {
                context.sender().sendRichMessage(tl.getEntry(),
                        FactionResolver.of(faction),
                        Placeholder.unparsed("amount", Econ.moneyString(faction.rentDebt())));
            }
        }

        if (!missed.isEmpty()) {
            context.sender().sendRichMessage(tl.getMissedHeader());
            for (Missed entry : missed) {
                context.sender().sendRichMessage(tl.getMissedEntry(),
                        FactionResolver.of(entry.faction()),
                        Placeholder.unparsed("count", String.valueOf(entry.count())),
                        Placeholder.unparsed("date", entry.mostRecent().toString()));
            }
        }
    }

    private record Missed(Faction faction, int count, LocalDate mostRecent) {
    }
}
