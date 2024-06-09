package dev.kitteh.factions.cmd;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.top.FTopBalanceValue;
import dev.kitteh.factions.cmd.top.FTopFacValPair;
import dev.kitteh.factions.cmd.top.FTopFoundedValue;
import dev.kitteh.factions.cmd.top.FTopGTIntValue;
import dev.kitteh.factions.cmd.top.FTopValue;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CmdTop extends FCommand {

    private static final Map<String, Function<Faction, FTopValue<?>>> topValueGenerators = new HashMap<>();

    static {
        topValueGenerators.put("members", f -> new FTopGTIntValue(f.getFPlayers().size()));
        topValueGenerators.put("start", f -> new FTopFoundedValue(f.getFoundedDate()));
        topValueGenerators.put("power", f -> new FTopGTIntValue(f.getPowerRounded()));
        topValueGenerators.put("land", f -> new FTopGTIntValue(f.getLandRounded()));
        topValueGenerators.put("online", f -> new FTopGTIntValue(f.getFPlayersWhereOnline(true).size()));
        Function<Faction, FTopValue<?>> money = f -> {
            double monies = FactionsPlugin.getInstance().conf().economy().isEnabled() ? Econ.getBalance(f) : 0;
            monies += f.getFPlayers().stream()
                    .mapToDouble(Econ::getBalance)
                    .sum();
            return new FTopBalanceValue(monies);
        };

        topValueGenerators.put("money", money);
        topValueGenerators.put("balance", money);
        topValueGenerators.put("bal", money);
    }

    public CmdTop() {
        super();
        this.aliases.add("top");
        this.aliases.add("t");

        this.requiredArgs.add("criteria");
        this.optionalArgs.put("page", "1");

        this.requirements = new CommandRequirements.Builder(Permission.TOP).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        // Can sort by: money, members, online, allies, enemies, power, land.
        // Get all Factions and remove non player ones.

        String criteria = context.argAsString(0);

        Function<Faction, FTopValue<?>> ftopGenerator = topValueGenerators.get(criteria);
        if (ftopGenerator == null || (!(FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl) && criteria.equalsIgnoreCase("power"))) {
            context.msg(TL.COMMAND_TOP_INVALID, criteria);
            return;
        }

        List<FTopFacValPair> sortedFactions = Factions.getInstance().getAllFactions().stream()
                .filter(Faction::isNormal)
                .map(f -> new FTopFacValPair(f, ftopGenerator.apply(f)))
                .sorted()
                .toList();

        int sortedFactionsCount = sortedFactions.size();

        final int pageheight = 9;
        int pagenumber = context.argAsInt(1, 1);
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
            String fac = context.sender instanceof Player ? faction.getRelationTo(context.fPlayer).getColor() + faction.getTag() : faction.getTag();
            lines.add(TL.COMMAND_TOP_LINE.format(rank, fac, fvpair.value.getDisplayString()));
            rank++;
        }

        context.sendMessage(lines);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TOP_DESCRIPTION;
    }
}
