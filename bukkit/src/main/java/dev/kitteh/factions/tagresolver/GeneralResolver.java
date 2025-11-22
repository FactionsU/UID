package dev.kitteh.factions.tagresolver;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Modifying;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.3.0")
@NullMarked
public class GeneralResolver extends HelperResolver {
    private static final GeneralResolver INSTANCE = new GeneralResolver();

    public static GeneralResolver resolver() {
        return INSTANCE;
    }

    private GeneralResolver() {
        super("fuuid");
    }

    @Override
    protected Tag solve(ArgumentQueue arguments, Context ctx) {
        String main = arguments.hasNext() ? arguments.pop().lowerValue() : "";

        return switch (main) {
            case "color" -> {
                if (arguments.hasNext()) {
                    yield switch (arguments.pop().lowerValue()) {
                        case "relation" -> {
                            if (arguments.hasNext()) {
                                yield tag(Relation.fromString(arguments.pop().lowerValue()).color());
                            } else {
                                yield empty();
                            }
                        }

                        case "role" -> {
                            if (arguments.hasNext() && Role.fromString(arguments.pop().lowerValue()) instanceof Role role) {
                                yield tag(role.color());
                            } else {
                                yield empty();
                            }
                        }

                        case "peaceful" -> tag(FactionsPlugin.instance().conf().colors().relations().getPeaceful());

                        case "safezone" -> tag(FactionsPlugin.instance().conf().colors().factions().getSafezone());
                        case "warzone" -> tag(FactionsPlugin.instance().conf().colors().factions().getWarzone());
                        case "wilderness" -> tag(FactionsPlugin.instance().conf().colors().factions().getWilderness());

                        default -> empty();
                    };
                } else {
                    yield empty();
                }
            }

            case "players_count_online" -> tag(Bukkit.getOnlinePlayers().size());
            case "players_factionless_online" -> tag(FPlayers.fPlayers().online().stream().filter(p -> !p.hasFaction()).count());
            case "players_factionless_total" -> tag(FPlayers.fPlayers().all().stream().filter(p -> !p.hasFaction()).count());
            case "players_faction_members_online" -> tag(FPlayers.fPlayers().online().stream().filter(FPlayer::hasFaction).count());
            case "players_faction_members_total" -> tag(FPlayers.fPlayers().all().stream().filter(FPlayer::hasFaction).count());
            case "players_tracked_total" -> tag(FPlayers.fPlayers().all().size());

            case "if_economy" -> tagToggle(Econ.shouldBeUsed(), arguments);
            case "if_banks" -> tagToggle(Econ.shouldBeUsedWithBanks(), arguments);

            case "title" -> (Modifying) (current, depth) -> {
                if (depth == 0) {
                    return TextUtil.titleize(current, ctx);
                } else {
                    return Component.empty();
                }
            };

            default -> empty();
        };
    }
}
