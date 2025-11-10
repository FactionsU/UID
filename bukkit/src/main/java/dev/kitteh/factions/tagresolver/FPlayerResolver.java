package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@ApiStatus.AvailableSince("4.0.0")
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

            case "title" -> observed.hasFaction() ? tag(observed.title()) : tag(Component.empty());
            case "name_and_title" -> observed.hasFaction() ? tag(observed.nameWithTitle()) : tag(observed.name());

            case "role_name" -> tagLegacy(observed.role().translation());
            case "role_prefix" -> tagLegacy(observed.role().getPrefix());

            case "uuid" -> tag(observed.uniqueId().toString());

            case "power_exact" -> tag(observed.power());
            case "power_rounded", "power" -> tag(observed.powerRounded());
            case "power_max_exact" -> tag(observed.powerMax());
            case "power_max_rounded", "power_max" -> tag(observed.powerMaxRounded());

            case "relation_name" -> tagLegacy(observed.relationTo(observer).translation());
            case "relation_color" -> tag(observed.relationTo(observer).color());

            case "kills" -> tag(observed.kills());
            case "deaths" -> tag(observed.deaths());

            case "last_seen" -> {
                long last = observed.lastLogin();
                long since = (last == 0 ? -1 : (System.currentTimeMillis() - last)) / 1000L;
                var tl = FactionsPlugin.instance().tl().placeholders().lastSeen();

                since = since - (since % tl.getIntervalSeconds());
                String duration = MiscUtil.durationString(since);

                if (since == -1) {
                    yield tagMini(tl.getUnknownText(), this);
                } else if (since >= tl.getRecentSeconds()) {
                    yield tagMini(tl.getOlderText(), Placeholder.unparsed("duration", duration), this);
                } else if (since >= tl.getTooRecentSeconds()) {
                    yield tagMini(tl.getRecentText(), Placeholder.unparsed("duration", duration), this);
                } else {
                    yield tagMini(tl.getTooRecentText(), Placeholder.unparsed("duration", duration), this);
                }
            }

            case "tooltip" -> tagTip(FactionsPlugin.instance().tl().placeholders().tooltips().getPlayer(), this);

            case "standing_in_faction" -> FactionResolver.of(observer, observed.lastStoodAt().faction()).solve(arguments, ctx);

            case "faction" -> FactionResolver.of(observer, observed.faction()).solve(arguments, ctx);

            case "space_if_faction" -> tag(observed.hasFaction() ? " " : "");

            case "papi", "papi_open" -> {
                if (!arguments.hasNext() || !FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integrations.PLACEHOLDERAPI)) {
                    yield tag(Component.empty());
                }
                String papi = arguments.pop().value();
                String result;
                if (papi.startsWith("rel_")) {
                    if (observerPlayer == null || !(observed.asPlayer() instanceof Player p)) {
                        yield (tag(Component.empty()));
                    }
                    result = PlaceholderAPI.setRelationalPlaceholders(p, this.observerPlayer, papiString(papi));
                } else {
                    result = PlaceholderAPI.setPlaceholders(observed.asOfflinePlayer(), papiString(papi));
                }
                yield main.equals("papi_open") ? tagLegacyIns(result) : tag(result);
            }

            default -> tag(Component.empty());
        };
    }

    private String papiString(String string) {
        return string.startsWith("%") ? string : '%' + string + '%';
    }
}
