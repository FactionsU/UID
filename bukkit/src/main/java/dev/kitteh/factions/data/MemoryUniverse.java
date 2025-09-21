package dev.kitteh.factions.data;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.LeveledValueProvider;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@NullMarked
public abstract class MemoryUniverse implements Universe {
    @SuppressWarnings({"FieldMayBeFinal"})
    protected static class Data {
        static class Grace {
            private long graceTimeEnd = 0L;
        }

        static class Upgrades {
            private List<String> disabled = new ArrayList<>();
            private Map<String, UpgradeSettings> settings = new HashMap<>();
        }

        private Grace grace = new Grace();
        private Upgrades upgrades = new Upgrades();
    }

    protected Data data = new Data();

    @Override
    public Duration graceRemaining() {
        long currentTime = System.currentTimeMillis();
        if (this.data.grace.graceTimeEnd < currentTime) {
            this.data.grace.graceTimeEnd = 0L;
        }
        return this.data.grace.graceTimeEnd == 0L ? Duration.ZERO : Duration.ofMillis(this.data.grace.graceTimeEnd - currentTime);
    }

    @Override
    public void graceRemaining(Duration graceRemaining) {
        this.data.grace.graceTimeEnd = graceRemaining.isZero() ? 0L : System.currentTimeMillis() + graceRemaining.toMillis();
    }

    @Override
    public boolean isUpgradeEnabled(Upgrade upgrade) {
        return this.data.upgrades.settings.containsKey(upgrade.name()) && !this.data.upgrades.disabled.contains(upgrade.name());
    }

    @Override
    public UpgradeSettings upgradeSettings(Upgrade upgrade) {
        return this.data.upgrades.settings.get(upgrade.name());
    }

    public abstract void forceSave(boolean sync);

    public void load() {
        this.loadData();
        Upgrades.defaults.forEach(upgrade -> {
            String name = upgrade.upgrade().name();
            UpgradeSettings settings = this.data.upgrades.settings.get(name);
            if (settings != null) {
                if (settings.findFlaw() instanceof String issue) {
                    AbstractFactionsPlugin.instance().getLogger().warning("Could not load upgrade setting for " + name + ": " + issue);
                    this.data.upgrades.settings.remove(name);
                }
            }
            if (!this.data.upgrades.settings.containsKey(name)) {
                if (upgrade.upgrade() == Upgrades.WARPS) {
                    upgrade = new UpgradeSettings(
                            Upgrades.WARPS,
                            Map.of(
                                    Upgrades.Variables.COUNT,  LeveledValueProvider.LevelMap.of(BigDecimal.valueOf(FactionsPlugin.instance().conf().commands().warp().getMaxWarps()))
                            ),
                            1,
                            1,
                            LeveledValueProvider.LevelMap.of(BigDecimal.ZERO)
                    );
                }
                this.data.upgrades.settings.put(name, upgrade);
                if (
                        !( // Negate the conditions for if-should-default-enable, which my brain finds easier to read.
                                (upgrade.upgrade() == Upgrades.FLIGHT && FactionsPlugin.instance().conf().commands().fly().isEnable()) ||
                                        (upgrade.upgrade() == Upgrades.WARPS && FactionsPlugin.instance().conf().commands().warp().getMaxWarps() > 0)
                        )
                ) {
                    this.data.upgrades.disabled.add(name);
                }
            }
        });
    }

    public void addDefaultsIfNotPresent(UpgradeSettings settings, boolean defaultDisabled) {
        if (UpgradeRegistry.getUpgrade(settings.upgrade().name()) != settings.upgrade()) {
            throw new IllegalArgumentException("Upgrade not registered");
        }
        if (this.data.upgrades.settings.containsKey(settings.upgrade().name())) {
            return;
        }
        if (settings.findFlaw() instanceof String issue) {
            throw new IllegalArgumentException(issue);
        }
        this.data.upgrades.settings.put(settings.upgrade().name(), settings);
        if (defaultDisabled) {
            this.data.upgrades.disabled.add(settings.upgrade().name());
        }
    }

    protected abstract void loadData();
}
