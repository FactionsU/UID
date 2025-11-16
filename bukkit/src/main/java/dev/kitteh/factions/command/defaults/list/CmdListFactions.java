package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.ComponentDispatcher;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import java.util.function.Function;

public class CmdListFactions implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            manager.command(
                    builder
                            .commandDescription(Cloudy.desc(TL.COMMAND_LIST_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LIST)))
                            .handler(this::handle)
            );
            manager.command(
                    builder.literal("factions")
                            .commandDescription(Cloudy.desc(TL.COMMAND_LIST_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LIST)))
                            .optional("page", IntegerParser.integerParser(1))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostList(), TL.COMMAND_LIST_TOLIST, TL.COMMAND_LIST_FORLIST)) {
            return;
        }

        List<Faction> factionList = Factions.factions().all();
        factionList.remove(Factions.factions().wilderness());
        factionList.remove(Factions.factions().safeZone());
        factionList.remove(Factions.factions().warZone());

        // remove exempt factions
        if (!context.sender().hasPermission(Permission.SHOW_BYPASS_EXEMPT)) {
            List<String> exemptFactions = FactionsPlugin.instance().conf().commands().show().getExempt();
            factionList.removeIf(next -> exemptFactions.contains(next.tag()));
        }

        // Sort by total followers first
        factionList.sort(this.compare(Faction::members));

        // Then sort by how many members are online now
        factionList.sort(this.compare(f -> f.membersOnline(true, fPlayer)));

        ArrayList<Component> lines = new ArrayList<>();

        factionList.addFirst(Factions.factions().wilderness());

        final int pageheight = 9;
        int pagenumber = context.getOrDefault("page", 1);
        int pagecount = (factionList.size() / pageheight) + 1;
        if (pagenumber > pagecount) {
            pagenumber = pagecount;
        } else if (pagenumber < 1) {
            pagenumber = 1;
        }
        int start = (pagenumber - 1) * pageheight;
        int end = start + pageheight;
        if (end > factionList.size()) {
            end = factionList.size();
        }

        AbstractFactionsPlugin plugin = AbstractFactionsPlugin.instance();

        String header = plugin.tl().commands().list().factions().getHeader();
        String footer = plugin.tl().commands().list().factions().getFooter();
        String factionlessEntry = plugin.tl().commands().list().factions().getFactionlessEntry();
        String factionEntry = plugin.tl().commands().list().factions().getEntry();

        if (!header.isEmpty()) {
            lines.add(Mini.parse(header,
                    Placeholder.unparsed("page_current", String.valueOf(pagenumber)),
                    Placeholder.unparsed("page_count", String.valueOf(pagecount))));
        }

        for (Faction faction : factionList.subList(start, end)) {
            lines.add(Mini.parse(faction.isWilderness() ? factionlessEntry : factionEntry, FactionResolver.of(fPlayer, faction)));
        }

        if (!footer.isEmpty()) {
            lines.add(Mini.parse(footer,
                    Placeholder.unparsed("page_current", String.valueOf(pagenumber)),
                    Placeholder.unparsed("page_count", String.valueOf(pagecount))));
        }

        lines.forEach(line -> ComponentDispatcher.send(context.sender().sender(), line));
    }

    private Comparator<Faction> compare(Function<Faction, ? extends Collection<?>> func) {
        return (f1, f2) -> {
            int f1Size = func.apply(f1).size();
            int f2Size = func.apply(f2).size();
            if (f1Size < f2Size) {
                return 1;
            } else if (f1Size > f2Size) {
                return -1;
            }
            return 0;
        };
    }
}