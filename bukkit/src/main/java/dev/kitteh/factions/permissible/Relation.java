package dev.kitteh.factions.permissible;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.integration.permcontext.Contexts;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.Set;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public enum Relation implements Permissible {
    MEMBER(4),
    ALLY(3),
    TRUCE(2),
    NEUTRAL(1),
    ENEMY(0);

    @Deprecated(forRemoval = true, since = "4.6.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public final int value;
    @Deprecated(forRemoval = true, since = "4.6.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    public final String nicename;
    private final Set<String> justMyNameInASet;

    Relation(final int value) {
        this.value = value;
        this.nicename = this.name();
        this.justMyNameInASet = Collections.singleton(this.name().toLowerCase());
    }

    public static Relation fromString(String s) {
        if (s == null) {
            return NEUTRAL;
        }
        if (s.equalsIgnoreCase(MEMBER.translation())) {
            return MEMBER;
        } else if (s.equalsIgnoreCase(ALLY.translation())) {
            return ALLY;
        } else if (s.equalsIgnoreCase(TRUCE.translation())) {
            return TRUCE;
        } else if (s.equalsIgnoreCase(ENEMY.translation())) {
            return ENEMY;
        } else {
            return switch (s.toUpperCase()) {
                case "MEMBER" -> MEMBER;
                case "ALLY" -> ALLY;
                case "TRUCE" -> TRUCE;
                case "ENEMY" -> ENEMY;
                case "NEUTRAL" -> NEUTRAL;
                default -> // If they somehow mess things up, go back to default behavior.
                        NEUTRAL;
            };
        }
    }

    @ApiStatus.AvailableSince("4.6.0")
    public int value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.translation();
    }

    @Override
    public String translation() {
        return switch(this) {
            case MEMBER -> FactionsPlugin.instance().tl().general().relations().getMember();
            case ALLY -> FactionsPlugin.instance().tl().general().relations().getAlly();
            case TRUCE -> FactionsPlugin.instance().tl().general().relations().getTruce();
            case NEUTRAL -> FactionsPlugin.instance().tl().general().relations().getNeutral();
            case ENEMY -> FactionsPlugin.instance().tl().general().relations().getEnemy();
        };
    }

    public String getPluralTranslation() {
        return switch(this) {
            case MEMBER -> FactionsPlugin.instance().tl().general().relations().getMembers();
            case ALLY -> FactionsPlugin.instance().tl().general().relations().getAllies();
            case TRUCE -> FactionsPlugin.instance().tl().general().relations().getTruces();
            case NEUTRAL -> FactionsPlugin.instance().tl().general().relations().getNeutrals();
            case ENEMY -> FactionsPlugin.instance().tl().general().relations().getEnemies();
        };
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

    @Override
    public TextColor color() {
        return switch (this) {
            case MEMBER -> FactionsPlugin.instance().conf().colors().relations().getMember();
            case ALLY -> FactionsPlugin.instance().conf().colors().relations().getAlly();
            case NEUTRAL -> FactionsPlugin.instance().conf().colors().relations().getNeutral();
            case TRUCE -> FactionsPlugin.instance().conf().colors().relations().getTruce();
            case ENEMY -> FactionsPlugin.instance().conf().colors().relations().getEnemy();
        };
    }

    public int getMax() {
        return switch (this) {
            case ALLY -> FactionsPlugin.instance().conf().factions().maxRelations().getAlly();
            case ENEMY -> FactionsPlugin.instance().conf().factions().maxRelations().getEnemy();
            case TRUCE -> FactionsPlugin.instance().conf().factions().maxRelations().getTruce();
            case NEUTRAL -> FactionsPlugin.instance().conf().factions().maxRelations().getNeutral();

            case MEMBER -> -1;
        };
    }

    public double getRelationCost() {
        if (isEnemy()) {
            return FactionsPlugin.instance().conf().economy().getCostEnemy();
        } else if (isAlly()) {
            return FactionsPlugin.instance().conf().economy().getCostAlly();
        } else if (isTruce()) {
            return FactionsPlugin.instance().conf().economy().getCostTruce();
        } else {
            return FactionsPlugin.instance().conf().economy().getCostNeutral();
        }
    }

    /**
     * Gets this enum name, in lower case, for fastest possible access for
     * {@link Contexts#TERRITORY_RELATION}
     *
     * @return an immutable set of just this name
     */
    public Set<String> getNameInASet() {
        return this.justMyNameInASet;
    }
}
