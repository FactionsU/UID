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
import java.util.Collections;
import java.util.List;

public class CmdRent implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().rent();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasFaction()).and(Cloudy.predicate(_ -> Econ.rentEnabled())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().rent();

        if (!Econ.rentEnabled()) { // How'd we get here?
            context.sender().sendRichMessage(tl.getDisabled());
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = sender.faction();
        if (!faction.isNormal()) { // How'd we get here?
            sender.sendRichMessage(tl.getNone());
            return;
        }

        if (faction.rentExempt()) {
            sender.sendRichMessage(tl.getExempt());
            return;
        }

        double amount = Econ.calculateRent(faction);
        double debt = faction.rentDebt();

        boolean shownPersonal = false;
        if (amount > 0 || debt > 0) {
            if (amount > 0) {
                sender.sendRichMessage(tl.getAmount(),
                        Placeholder.unparsed("amount", Econ.moneyString(amount)),
                        Placeholder.unparsed("claims", String.valueOf(faction.claimCount())));
            }
            if (debt > 0) {
                sender.sendRichMessage(tl.getDebtOwn(), Placeholder.unparsed("amount", Econ.moneyString(debt)));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextCollection = LocalDate.now().plusDays(1).atStartOfDay();
            sender.sendRichMessage(tl.getCollection(),
                    Placeholder.unparsed("time", MiscUtil.durationString(Duration.between(now, nextCollection))));

            double owed = amount + debt;
            if (owed > 0 && !Econ.has(faction, owed)) {
                sender.sendRichMessage(tl.getCannotAfford(),
                        Placeholder.unparsed("amount", Econ.moneyString(owed)),
                        Placeholder.unparsed("balance", Econ.moneyString(Econ.getBalance(faction))));
            }
            shownPersonal = true;
        }

        boolean shownAdmin = false;
        if (sender.role() == Role.ADMIN) {
            List<LocalDate> missed = faction.missedRentDates();
            if (!missed.isEmpty()) {
                sender.sendRichMessage(tl.getMissed(),
                        Placeholder.unparsed("count", String.valueOf(missed.size())),
                        Placeholder.unparsed("date", Collections.max(missed).toString()));
                shownAdmin = true;
            }
        }

        if (!shownPersonal && !shownAdmin) {
            sender.sendRichMessage(tl.getNone());
        }
    }
}
