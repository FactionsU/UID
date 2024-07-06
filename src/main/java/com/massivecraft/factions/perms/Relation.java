package com.massivecraft.factions.perms;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.util.TL;
import com.massivecraft.factions.util.TextUtil;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.Set;

public enum Relation implements Permissible {
    MEMBER(4, TL.RELATION_MEMBER_SINGULAR.toString()),
    ALLY(3, TL.RELATION_ALLY_SINGULAR.toString()),
    TRUCE(2, TL.RELATION_TRUCE_SINGULAR.toString()),
    NEUTRAL(1, TL.RELATION_NEUTRAL_SINGULAR.toString()),
    ENEMY(0, TL.RELATION_ENEMY_SINGULAR.toString());

    public final int value;
    public final String nicename;
    private final Set<String> justMyNameInASet;

    Relation(final int value, final String nicename) {
        this.value = value;
        this.nicename = nicename;
        this.justMyNameInASet = Collections.singleton(this.name().toLowerCase());
    }

    public static Relation fromString(String s) {
        if (s == null) {
            return NEUTRAL;
        }
        if (s.equalsIgnoreCase(MEMBER.nicename)) {
            return MEMBER;
        } else if (s.equalsIgnoreCase(ALLY.nicename)) {
            return ALLY;
        } else if (s.equalsIgnoreCase(TRUCE.nicename)) {
            return TRUCE;
        } else if (s.equalsIgnoreCase(ENEMY.nicename)) {
            return ENEMY;
        } else {
            return switch (s.toUpperCase()) {
                case "MEMBER" -> MEMBER;
                case "ALLY" -> ALLY;
                case "TRUCE" -> TRUCE;
                case "ENEMY" -> ENEMY;
                default -> // If they somehow mess things up, go back to default behavior.
                        NEUTRAL;
            };
        }
    }

    @Override
    public String toString() {
        return this.nicename;
    }

    @Override
    public String getTranslation() {
        try {
            return TL.valueOf("RELATION_" + name() + "_SINGULAR").toString();
        } catch (IllegalArgumentException e) {
            return toString();
        }
    }

    public String getPluralTranslation() {
        for (TL t : TL.values()) {
            if (t.name().equalsIgnoreCase("RELATION_" + name() + "_PLURAL")) {
                return t.toString();
            }
        }
        return toString();
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isAlly() {
        return this == ALLY;
    }

    public boolean isTruce() {
        return this == TRUCE;
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public boolean isEnemy() {
        return this == ENEMY;
    }

    public boolean isAtLeast(Relation relation) {
        return this.value >= relation.value;
    }

    public boolean isAtMost(Relation relation) {
        return this.value <= relation.value;
    }

    public ChatColor getColor() {
        return TextUtil.getClosest(this.getTextColor());
    }

    @Override
    public TextColor getTextColor() {
        return switch (this) {
            case MEMBER -> FactionsPlugin.getInstance().conf().colors().relations().getMember();
            case ALLY -> FactionsPlugin.getInstance().conf().colors().relations().getAlly();
            case NEUTRAL -> FactionsPlugin.getInstance().conf().colors().relations().getNeutral();
            case TRUCE -> FactionsPlugin.getInstance().conf().colors().relations().getTruce();
            default -> FactionsPlugin.getInstance().conf().colors().relations().getEnemy();
        };
    }

    public int getMax() {
        return switch (this) {
            case ALLY -> FactionsPlugin.getInstance().conf().factions().maxRelations().getAlly();
            case ENEMY -> FactionsPlugin.getInstance().conf().factions().maxRelations().getEnemy();
            case TRUCE -> FactionsPlugin.getInstance().conf().factions().maxRelations().getTruce();
            default -> FactionsPlugin.getInstance().conf().factions().maxRelations().getNeutral();
        };
    }

    public double getRelationCost() {
        if (isEnemy()) {
            return FactionsPlugin.getInstance().conf().economy().getCostEnemy();
        } else if (isAlly()) {
            return FactionsPlugin.getInstance().conf().economy().getCostAlly();
        } else if (isTruce()) {
            return FactionsPlugin.getInstance().conf().economy().getCostTruce();
        } else {
            return FactionsPlugin.getInstance().conf().economy().getCostNeutral();
        }
    }

    /**
     * Gets this enum name, in lower case, for fastest possible access for
     * {@link com.massivecraft.factions.integration.permcontext.Contexts#TERRITORY_RELATION}
     *
     * @return an immutable set of just this name
     */
    public Set<String> getNameInASet() {
        return this.justMyNameInASet;
    }
}
