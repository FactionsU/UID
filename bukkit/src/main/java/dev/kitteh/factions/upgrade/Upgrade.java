package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.util.Mini;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@NullMarked
public interface Upgrade {
    record SimpleImpl(String name, Function<TranslationsConfig.Upgrades, TranslationsConfig.Upgrades.UpgradeDetail> tl, int maxLevel,
                      Set<UpgradeVariable> variables) implements Upgrade.Impl {
    }

    record ReactiveImpl(String name, Function<TranslationsConfig.Upgrades, TranslationsConfig.Upgrades.UpgradeDetail> tl, int maxLevel,
                        Set<UpgradeVariable> variables, Reactor reactor) implements Upgrade.Impl {
        @Override
        public void onChange(Faction faction, int oldLevel, int newLevel) {
            this.reactor.onChange(faction, oldLevel, newLevel);
        }
    }

    record Simple(String name, Component nameComponent, Component description, BiFunction<UpgradeSettings, Integer, Component> detailsFunction, int maxLevel,
                  Set<UpgradeVariable> variables) implements Upgrade {
        @Override
        public Component details(UpgradeSettings settings, int level) {
            return this.detailsFunction.apply(settings, level);
        }
    }

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

    @FunctionalInterface
    interface Reactor {
        Reactor UPDATE_COMMANDS = (faction, oldLevel, newLevel) -> {
            if (oldLevel == 0 || newLevel == 0) {
                faction.membersOnlineAsPlayers().forEach(Player::updateCommands);
            }
        };

        void onChange(Faction faction, int oldLevel, int newLevel);
    }

    String name();

    Component nameComponent();

    Component description();

    Component details(UpgradeSettings settings, int level);

    int maxLevel();

    Set<UpgradeVariable> variables();

    default void onChange(Faction faction, int oldLevel, int newLevel) {
    }
}
