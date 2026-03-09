package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public final class FPlayerResolver extends ObservedResolver {
    @ApiStatus.AvailableSince("4.5.0")
    public static FPlayerResolver of(String name, FPlayer observed) {
        return new FPlayerResolver(name, observed);
    }

    public static FPlayerResolver of(String name, @Nullable FPlayer forcedObserver, FPlayer observed) {
        return new FPlayerResolver(name, forcedObserver, observed);
    }

    @Deprecated(forRemoval = true, since = "4.5.0")
    public static FPlayerResolver of(String name, @Nullable Player forcedObserver, FPlayer observed) {
        return new FPlayerResolver(name, forcedObserver, observed);
    }

    private final FPlayer observed;

    private FPlayerResolver(String name, FPlayer observed) {
        super(name);
        this.observed = observed;
    }

    private FPlayerResolver(String name, @Nullable FPlayer forcedObserver, FPlayer observed) {
        super(name, forcedObserver);
        this.observed = observed;
    }

    private FPlayerResolver(String name, @Nullable Player forcedObserver, FPlayer observed) {
        super(name, forcedObserver);
        this.observed = observed;
    }

    @Override
    protected Tag solve(ArgumentQueue arguments, Context ctx) {
        String main = arguments.hasNext() ? arguments.pop().lowerValue() : "";

        return switch (main) {
            case "", "name_decorated" -> tagLegacy(observed.describeToLegacy(this.observer(ctx)));

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

            case "relation_name" -> tagLegacy(observed.relationTo(this.observer(ctx)).translation());
            case "relation_color" -> tag(observed.relationTo(this.observer(ctx)).color());

            case "kills" -> tag(observed.kills());
            case "deaths" -> tag(observed.deaths());

            case "last_seen" -> {
                long last = observed.lastLogin();
                long since = (last == 0 ? -1 : (System.currentTimeMillis() - last)) / 1000L;
                var tl = FactionsPlugin.instance().tl().placeholders().lastSeen();

                since = since - (since % tl.getIntervalSeconds());
                String duration = MiscUtil.durationString(since);

                if (since == -1) {
                    yield tagMini(tl.getUnknownText(), this.observer(ctx), this);
                } else if (since >= tl.getRecentSeconds()) {
                    yield tagMini(tl.getOlderText(), this.observer(ctx), Placeholder.unparsed("duration", duration), this);
                } else if (since >= tl.getTooRecentSeconds()) {
                    yield tagMini(tl.getRecentText(), this.observer(ctx), Placeholder.unparsed("duration", duration), this);
                } else {
                    yield tagMini(tl.getTooRecentText(), this.observer(ctx), Placeholder.unparsed("duration", duration), this);
                }
            }

            case "scoreboard_map" -> {
                if (arguments.hasNext()) {
                    OptionalInt index = arguments.pop().asInt();
                    if (index.isPresent()) {
                        List<Component> board = Instances.BOARD.getScoreboardMap(observed);
                        if (index.getAsInt() < board.size()) {
                            yield tag(board.get(index.getAsInt()));
                        }
                    }
                }
                yield empty();
            }

            case "tooltip" -> tagTip(FactionsPlugin.instance().tl().placeholders().tooltips().getPlayer(), this.observer(ctx), this);

            case "standing_in_faction" -> FactionResolver.of(observed.lastStoodAt().faction()).solve(arguments, ctx);

            case "faction" -> FactionResolver.of(observed.faction()).solve(arguments, ctx);

            case "space_if_faction" -> tag(observed.hasFaction() ? " " : "");

            case "papi", "papi_open", "papi_mini" -> {
                if (!arguments.hasNext() || !FactionsPlugin.instance().integrationManager().isEnabled(IntegrationManager.Integrations.PLACEHOLDERAPI)) {
                    yield tag(Component.empty());
                }
                String papi = arguments.pop().value();
                String result;
                if (papi.startsWith("rel_")) {
                    Player observerPlayer = this.observer(ctx) instanceof FPlayer fp ? fp.asPlayer() : null;
                    if (observerPlayer == null || !(observed.asPlayer() instanceof Player p)) {
                        yield (tag(Component.empty()));
                    }
                    result = PlaceholderAPI.setRelationalPlaceholders(p, observerPlayer, papiString(papi));
                } else {
                    result = PlaceholderAPI.setPlaceholders(observed.asOfflinePlayer(), papiString(papi));
                }
                yield main.equals("papi_open") ? tagLegacyIns(result) : (main.equals("papi_mini") ? Tag.preProcessParsed(result) : tag(result));
            }

            default -> tag(Component.empty());
        };
    }

    private String papiString(String string) {
        return string.startsWith("%") ? string : '%' + string + '%';
    }
}
