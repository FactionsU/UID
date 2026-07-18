package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CmdDues implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().dues();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasFaction()).and(Cloudy.predicate(_ -> Econ.duesEnabled())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().dues();

        if (!Econ.duesEnabled()) {
            context.sender().sendRichMessage(Confs.tl().commands().set().dues().getDisabled());
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = sender.faction();
        if (!faction.isNormal()) { // How'd we get here?
            sender.sendRichMessage(tl.getNone());
            return;
        }

        double amount = faction.dues(sender.role());
        double debt = sender.duesDebt();

        boolean shownPersonal = false;
        if (amount > 0 || debt > 0) {
            if (amount > 0) {
                sender.sendRichMessage(tl.getAmount(),
                        Placeholder.unparsed("amount", Econ.moneyString(amount)),
                        Placeholder.unparsed("role", sender.role().translation()));
            }
            if (debt > 0) {
                sender.sendRichMessage(tl.getDebtOwn(), Placeholder.unparsed("amount", Econ.moneyString(debt)));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextCollection = LocalDate.now().plusDays(1).atStartOfDay();
            sender.sendRichMessage(tl.getCollection(),
                    Placeholder.unparsed("time", MiscUtil.durationString(Duration.between(now, nextCollection))));

            double owed = amount + debt;
            if (owed > 0 && !Econ.has(sender, owed)) {
                sender.sendRichMessage(tl.getCannotAfford(),
                        Placeholder.unparsed("amount", Econ.moneyString(owed)),
                        Placeholder.unparsed("balance", Econ.getFriendlyBalance(sender)));
            }
            shownPersonal = true;
        }

        boolean shownAdmin = false;
        if (sender.role() == Role.ADMIN) {
            // Members who owe unpaid dues (accumulated under the debt policy).
            List<FPlayer> debtors = faction.members().stream()
                    .filter(member -> member.duesDebt() > 0)
                    .sorted(Comparator.<FPlayer>comparingDouble(FPlayer::duesDebt).reversed())
                    .toList();
            if (!debtors.isEmpty()) {
                sender.sendRichMessage(tl.getDebtHeader());
                for (FPlayer debtor : debtors) {
                    sender.sendRichMessage(tl.getDebtEntry(),
                            Placeholder.unparsed("player", debtor.name()),
                            Placeholder.unparsed("amount", Econ.moneyString(debtor.duesDebt())));
                }
                shownAdmin = true;
            }

            // Members who have missed dues (recorded under the record policy), most recent first.
            List<Missed> missed = new ArrayList<>();
            for (FPlayer member : faction.members()) {
                List<LocalDate> dates = member.missedDuesDates();
                if (!dates.isEmpty()) {
                    missed.add(new Missed(member, dates.size(), Collections.max(dates)));
                }
            }
            missed.sort(Comparator.comparing(Missed::mostRecent).reversed());
            if (!missed.isEmpty()) {
                sender.sendRichMessage(tl.getMissedHeader());
                for (Missed entry : missed) {
                    sender.sendRichMessage(tl.getMissedEntry(),
                            Placeholder.unparsed("player", entry.player().name()),
                            Placeholder.unparsed("count", String.valueOf(entry.count())),
                            Placeholder.unparsed("date", entry.mostRecent().toString()));
                }
                shownAdmin = true;
            }
        }

        if (!shownPersonal && !shownAdmin) {
            sender.sendRichMessage(tl.getNone());
        }
    }

    private record Missed(FPlayer player, int count, LocalDate mostRecent) {
    }
}
