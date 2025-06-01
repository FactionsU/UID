package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.util.TL;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class FPlayerResolver extends ObservedResolver {
    public static FPlayerResolver of(String name, @Nullable FPlayer observer, FPlayer observed) {
        return new FPlayerResolver(name, observer, observed);
    }

    public static FPlayerResolver of(String name, @Nullable Player observer, FPlayer observed) {
        return new FPlayerResolver(name, observer, observed);
    }

    private final FPlayer observed;

    private FPlayerResolver(String name, @Nullable FPlayer observer, FPlayer observed) {
        super(name, observer);
        this.observed = observed;
    }

    private FPlayerResolver(String name, @Nullable Player observer, FPlayer observed) {
        super(name, observer);
        this.observed = observed;
    }

    @Override
    protected Tag solve(ArgumentQueue arguments, Context ctx) {
        String main = arguments.hasNext() ? arguments.pop().lowerValue() : "";

        return switch (main) {
            case "", "name_decorated" -> tagLegacy(observed.describeToLegacy(observer));

            case "name" -> tag(observed.name());

            case "role_name" -> tagLegacy(observed.role().translation());
            case "role_prefix" -> tagLegacy(observed.role().getPrefix());

            case "uuid" -> tag(observed.uniqueId().toString());

            case "power_exact" -> tag(observed.power());
            case "power_rounded" -> tag(observed.powerRounded());
            case "power_max_exact" -> tag(observed.powerMax());
            case "power_max_rounded" -> tag(observed.powerMaxRounded());

            case "relation_name" -> tagLegacy(observed.relationTo(observer).translation());
            case "relation_color" -> tag(observed.relationTo(observer).color());

            case "standing_in_faction" -> FactionResolver.of(observer, observed.lastStoodAt().faction()).solve(arguments, ctx);

            case "faction" -> FactionResolver.of(observer, observed.faction()).solve(arguments, ctx);

            case "space_if_faction" -> tag(observed.hasFaction() ? " " : "");

            case "papi" -> {
                if (!arguments.hasNext() || !FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integration.PLACEHOLDERAPI)) {
                    yield tag(Component.empty());
                }
                String papi = arguments.pop().value();
                if (papi.startsWith("rel_")) {
                    if (observerPlayer == null || !(observed.asPlayer() instanceof Player p)) {
                        yield (tag(Component.empty()));
                    }
                    yield tagLegacy(PlaceholderAPI.setRelationalPlaceholders(p, this.observerPlayer, papiString(papi)));
                }
                yield tagLegacy(PlaceholderAPI.setPlaceholders(observed.asOfflinePlayer(), papiString(papi)));
            }

            default -> tag(Component.empty());
        };
    }

    private String papiString(String string) {
        return string.startsWith("%") ? string : '%' + string + '%';
    }
}
