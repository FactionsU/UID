package dev.kitteh.factions.integration.permcontext;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.permissible.Role;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default FactionsUUID contexts.
 */
@NullMarked
public enum Contexts implements Context {
    FACTION_ID((player) -> {
        FPlayer p = FPlayers.fPlayers().get(player);
        return Set.of(String.valueOf(p.hasFaction() ? p.faction().id() : Factions.ID_WILDERNESS));
    }, Set.of(String.valueOf(Factions.ID_WILDERNESS))),
    IS_PEACEFUL((player) -> {
        FPlayer p = FPlayers.fPlayers().get(player);
        return Set.of(p.hasFaction() && p.faction().peaceful() ? "true" : "false");
    }, Set.of("true", "false")),
    IS_PERMANENT((player) -> {
        FPlayer p = FPlayers.fPlayers().get(player);
        return Set.of(p.hasFaction() && p.faction().permanent() ? "true" : "false");
    }, Set.of("true", "false")),
    TERRITORY_RELATION((player) ->
            FPlayers.fPlayers().get(player).relationTo(Board.board().factionAt(new FLocation(player.getLocation()))).getNameInASet(),
            Arrays.stream(Relation.values()).map(relation -> relation.name().toLowerCase()).collect(Collectors.toSet())),
    TERRITORY_IS_SAFEZONE((player) ->
            Set.of(new FLocation(player).faction().isSafeZone() ? "true" : "false"),
            Set.of("true", "false")),
    TERRITORY_IS_WILDERNESS((player) ->
            Set.of(new FLocation(player).faction().isWilderness() ? "true" : "false"),
            Set.of("true", "false")),
    TERRITORY_IS_WARZONE((player) ->
            Set.of(new FLocation(player).faction().isWarZone() ? "true" : "false"),
            Set.of("true", "false")),
    TERRITORY_ID((player) ->
            Set.of(String.valueOf(new FLocation(player).faction().id())),
            Set.of(String.valueOf(Factions.ID_WILDERNESS))),
    ROLE_AT_LEAST((player) ->
    {
        FPlayer p = FPlayers.fPlayers().get(player);
        return p.hasFaction() ? p.role().getRoleNamesAtOrBelow() : Collections.emptySet();
    },
            Arrays.stream(Role.values()).map(role -> role.name().toLowerCase()).collect(Collectors.toSet())),
    ROLE_AT_MOST((player) ->
    {
        FPlayer p = FPlayers.fPlayers().get(player);
        return p.hasFaction() ? p.role().getRoleNamesAtOrAbove() : Collections.emptySet();
    },
            Arrays.stream(Role.values()).map(role -> role.name().toLowerCase()).collect(Collectors.toSet()));


    /**
     * The FactionsUUID namespace. Should not be used by another plugin.
     */
    public static final String FACTIONSUUID_NAMESPACE = "factionsuuid";

    private final Function<Player, Set<String>> function;
    private final String name;
    private final Set<String> possibilities;

    Contexts(Function<Player, Set<String>> function, Set<String> possibilities) {
        this.function = function;
        this.name = this.name().toLowerCase().replace('_', '-');
        this.possibilities = Collections.unmodifiableSet(possibilities);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return FACTIONSUUID_NAMESPACE;
    }

    @Override
    public Set<String> getPossibleValues() {
        return this.possibilities;
    }

    @Override
    public Set<String> getValues(Player player) {
        return this.function.apply(player);
    }
}
