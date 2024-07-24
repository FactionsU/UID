package com.massivecraft.factions.integration.permcontext;

import com.google.common.collect.ImmutableSet;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default FactionsUUID contexts.
 */
public enum Contexts implements Context {
    FACTION_ID((player) -> {
        FPlayer p = FPlayers.getInstance().getByPlayer(player);
        return ImmutableSet.of(p.hasFaction() ? String.valueOf(p.getFactionIntId()) : "0");
    }, ImmutableSet.of("0")),
    IS_PEACEFUL((player) -> {
        FPlayer p = FPlayers.getInstance().getByPlayer(player);
        return ImmutableSet.of(p.hasFaction() && p.getFaction().isPeaceful() ? "true" : "false");
    }, ImmutableSet.of("true", "false")),
    IS_PERMANENT((player) -> {
        FPlayer p = FPlayers.getInstance().getByPlayer(player);
        return ImmutableSet.of(p.hasFaction() && p.getFaction().isPermanent() ? "true" : "false");
    }, ImmutableSet.of("true", "false")),
    TERRITORY_RELATION((player) ->
            FPlayers.getInstance().getByPlayer(player).getRelationTo(Board.getInstance().getFactionAt(new FLocation(player.getLocation()))).getNameInASet(),
            Arrays.stream(Relation.values()).map(relation -> relation.name().toLowerCase()).collect(Collectors.toSet())),
    ROLE_AT_LEAST((player) ->
    {
        FPlayer p = FPlayers.getInstance().getByPlayer(player);
        return p.hasFaction() ? p.getRole().getRoleNamesAtOrBelow() : Collections.emptySet();
    },
            Arrays.stream(Role.values()).map(role -> role.name().toLowerCase()).collect(Collectors.toSet())),
    ROLE_AT_MOST((player) ->
    {
        FPlayer p = FPlayers.getInstance().getByPlayer(player);
        return p.hasFaction() ? p.getRole().getRoleNamesAtOrAbove() : Collections.emptySet();
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
