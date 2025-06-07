package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.top.FTopBalanceValue;
import dev.kitteh.factions.command.defaults.top.FTopFacValPair;
import dev.kitteh.factions.command.defaults.top.FTopFoundedValue;
import dev.kitteh.factions.command.defaults.top.FTopGTIntValue;
import dev.kitteh.factions.command.defaults.top.FTopValue;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CmdTop implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("top")
                        .commandDescription(Cloudy.desc(TL.COMMAND_TOP_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.TOP)))
                        .required("criteria", StringParser.stringParser(), SuggestionProvider.suggestingStrings(topValueGenerators.keySet()))
                        .optional("page", IntegerParser.integerParser(1))
                        .handler(this::handle)
        );
    }

    private static final Map<String, Function<Faction, FTopValue<?>>> topValueGenerators = new HashMap<>();

    static {
        topValueGenerators.put("members", f -> new FTopGTIntValue(f.members().size()));
        topValueGenerators.put("start", f -> new FTopFoundedValue(f.founded().toEpochMilli()));
        topValueGenerators.put("power", f -> new FTopGTIntValue(f.power()));
        topValueGenerators.put("land", f -> new FTopGTIntValue(f.claimCount()));
        topValueGenerators.put("online", f -> new FTopGTIntValue(f.membersOnline(true).size()));
        Function<Faction, FTopValue<?>> money = f -> {
            double monies = FactionsPlugin.instance().conf().economy().isEnabled() ? Econ.getBalance(f) : 0;
            monies += f.members().stream()
                    .mapToDouble(Econ::getBalance)
                    .sum();
            return new FTopBalanceValue(monies);
        };

        topValueGenerators.put("money", money);
        topValueGenerators.put("balance", money);
        topValueGenerators.put("bal", money);
    }

    private void handle(CommandContext<Sender> context) {
        // Can sort by: money, members, online, allies, enemies, power, land.
        // Get all Factions and remove non player ones.

        String criteria = context.get("criteria");
        criteria = criteria.toLowerCase();

        Function<Faction, FTopValue<?>> ftopGenerator = topValueGenerators.get(criteria);
        if (ftopGenerator == null || (!(FactionsPlugin.instance().landRaidControl() instanceof PowerControl) && criteria.equalsIgnoreCase("power"))) {
            context.sender().msg(TL.COMMAND_TOP_INVALID, criteria);
            return;
        }

        List<FTopFacValPair> sortedFactions = Factions.factions().all().stream()
                .filter(Faction::isNormal)
                .map(f -> new FTopFacValPair(f, ftopGenerator.apply(f)))
                .sorted()
                .toList();

        int sortedFactionsCount = sortedFactions.size();

        final int pageheight = 9;
        int pagenumber = context.getOrDefault("page", 1);
        int pagecount = (sortedFactionsCount / pageheight) + 1;
        if (pagenumber > pagecount) {
            pagenumber = pagecount;
        } else if (pagenumber < 1) {
            pagenumber = 1;
        }
        int start = (pagenumber - 1) * pageheight;
        int end = start + pageheight;
        if (end > sortedFactionsCount) {
            end = sortedFactionsCount;
        }

        // One line for the header, end - start lines for the factions
        List<String> lines = new ArrayList<>(1 + end - start);

        lines.add(TL.COMMAND_TOP_TOP.format(criteria.toUpperCase(), pagenumber, pagecount));

        int rank = start + 1;
        for (FTopFacValPair fvpair : sortedFactions.subList(start, end)) {
            // Get the relation color if player is executing this.
            Faction faction = fvpair.faction;
            String fac = context.sender().isPlayer() ? faction.colorLegacyStringTo(context.sender().fPlayerOrNull()) + faction.tag() : faction.tag();
            lines.add(TL.COMMAND_TOP_LINE.format(rank, fac, fvpair.value.getDisplayString()));
            rank++;
        }

        lines.forEach(context.sender().sender()::sendMessage);
    }
}
