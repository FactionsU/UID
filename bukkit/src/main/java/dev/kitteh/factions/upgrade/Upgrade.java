package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An upgrade.
 * Custom upgrades can be created, potentially using optional helper records {@link Simple} and {@link Reactive}.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public interface Upgrade {
    @ApiStatus.Internal
    record SimpleImpl(String name, Function<TranslationsConfig.Upgrades, TranslationsConfig.Upgrades.UpgradeDetail> tl, int maxLevel,
                      Set<UpgradeVariable> variables) implements Upgrade.Impl {
    }

    @ApiStatus.Internal
    record ReactiveImpl(String name, Function<TranslationsConfig.Upgrades, TranslationsConfig.Upgrades.UpgradeDetail> tl, int maxLevel,
                        Set<UpgradeVariable> variables, Reactor reactor) implements Upgrade.Impl {
        @Override
        public void onChange(Faction faction, int oldLevel, int newLevel) {
            this.reactor.onChange(faction, oldLevel, newLevel);
        }
    }

    /**
     * Optional helper record for creating a non-reactive upgrade.
     *
     * @param name simple name
     * @param nameComponent decorated name
     * @param description decorated description
     * @param detailsFunction function converting upgrade settings to a decorated detail on a given level
     * @param maxLevel max level configurable
     * @param variables variables used
     */
    record Simple(String name, Component nameComponent, Component description, BiFunction<UpgradeSettings, Integer, Component> detailsFunction, int maxLevel,
                  Set<UpgradeVariable> variables) implements Upgrade {
        @Override
        public Component details(UpgradeSettings settings, int level) {
            return this.detailsFunction.apply(settings, level);
        }
    }

    /**
     * Optional helper record for creating a reactive upgrade.
     *
     * @param name simple name
     * @param nameComponent decorated name
     * @param description decorated description
     * @param detailsFunction function converting upgrade settings to a decorated detail on a given level
     * @param maxLevel max level configurable
     * @param variables variables used
     * @param reactor response to a faction changing upgrade level
     */
    record Reactive(String name, Component nameComponent, Component description, BiFunction<UpgradeSettings, Integer, Component> detailsFunction, int maxLevel,
                    Set<UpgradeVariable> variables, Reactor reactor) implements Upgrade {
        @Override
        public Component details(UpgradeSettings settings, int level) {
            return this.detailsFunction.apply(settings, level);
        }

        @Override
        public void onChange(Faction faction, int oldLevel, int newLevel) {
            this.reactor.onChange(faction, oldLevel, newLevel);
        }
    }

    @ApiStatus.Internal
    interface Impl extends Upgrade {
        @Override
        default Component nameComponent() {
            return Mini.parse(tl().apply(FactionsPlugin.instance().tl().upgrades()).getName());
        }

        @Override
        default Component description() {
            return Mini.parse(tl().apply(FactionsPlugin.instance().tl().upgrades()).getDescription());
        }

        @Override
        default Component details(UpgradeSettings settings, int level) {
            return Mini.parse(
                    tl().apply(FactionsPlugin.instance().tl().upgrades()).getDetail(),
                    TagResolver.resolver(this.variables().stream().map(up -> Placeholder.unparsed(up.name(), up.formatter().apply(settings.valueAt(up, level)))).toList())
            );
        }

        Function<TranslationsConfig.Upgrades, TranslationsConfig.Upgrades.UpgradeDetail> tl();
    }

    /**
     * Helper interface for responding to level changes.
     */
    @FunctionalInterface
    interface Reactor {
        /**
         * Helper reactor for simply pushing a {@link Player#updateCommands()} to all online members when level changes to or from level 0.
         */
        Reactor UPDATE_COMMANDS = (faction, oldLevel, newLevel) -> {
            if (oldLevel == 0 || newLevel == 0) {
                faction.membersOnlineAsPlayers().forEach(Player::updateCommands);
            }
        };

        void onChange(Faction faction, int oldLevel, int newLevel);
    }

    /**
     * Gets the upgrade's name as a simple string.
     *
     * @return name
     */
    String name();

    /**
     * Gets the upgrade's name as a decorated component.
     *
     * @return decorated name
     */
    Component nameComponent();

    /**
     * Get's the upgrade's generic description as a decorated component.
     *
     * @return decorated description
     */
    Component description();

    /**
     * Gets details about a specific level of the upgrade, using available upgrade settings.
     *
     * @param settings upgrade settings
     * @param level level
     * @return decorated details
     */
    Component details(UpgradeSettings settings, int level);

    /**
     * Gets the max level this upgrade could possible be configured, typically 1 for boolean upgrades and {@link Integer#MAX_VALUE} otherwise.
     *
     * @return max level configurable
     */
    int maxLevel();

    /**
     * Gets the variables this upgrade tracks for its levels, if any.
     *
     * @return set of any variables this upgrade uses
     */
    Set<UpgradeVariable> variables();

    /**
     * Called by the plugin when a faction changes level of this upgrade, potentially executing code.
     *
     * @param faction faction experiencing level change
     * @param oldLevel old level
     * @param newLevel new level
     */
    default void onChange(Faction faction, int oldLevel, int newLevel) {
    }
}
