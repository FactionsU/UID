package dev.kitteh.factions.data;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.annotation.NoFinalFields;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.config.transition.Transitioner;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.upgrade.LeveledValueProvider;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeRegistry;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import dev.kitteh.factions.util.MiscUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@NullMarked
public abstract class MemoryUniverse implements Universe {
    @NoFinalFields
    @SuppressWarnings("FieldMayBeFinal")
    protected static class Data {
        static class Grace {
            private long graceTimeEnd = 0L;
        }

        static class Shields {
            private LocalTime lastRunShieldCheck = null;
        }

        static class Dues {
            private @Nullable LocalDate lastCollectionDate = null;
        }

        static class Upgrades {
            private List<String> disabled = new ArrayList<>();
            private Map<String, UpgradeSettings> settings = new HashMap<>();
        }

        private Grace grace = new Grace();
        private Shields shields = new Shields();
        private Dues dues = new Dues();
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
    public LocalTime shieldScheduleLastTimeChecked() {
        return this.data.shields.lastRunShieldCheck;
    }

    public void shieldScheduleBumpNextTime() {
        LocalTime current = this.data.shields.lastRunShieldCheck;
        this.data.shields.lastRunShieldCheck = (current == null ? MiscUtil.floorToHalfHour(LocalTime.now()) : current).plusMinutes(30);
    }

    @Override
    public @Nullable LocalDate lastDuesCollectionDate() {
        return this.data.dues.lastCollectionDate;
    }

    @Override
    public void lastDuesCollectionDate(LocalDate date) {
        this.data.dues.lastCollectionDate = date;
    }

    @Override
    public boolean isUpgradeEnabled(Upgrade upgrade) {
        return this.data.upgrades.settings.containsKey(upgrade.name()) && !this.data.upgrades.disabled.contains(upgrade.name());
    }

    @Override
    public UpgradeSettings upgradeSettings(Upgrade upgrade) {
        return this.data.upgrades.settings.get(upgrade.name());
    }

    @Override
    public void upgradeEnabled(Upgrade upgrade, boolean enabled) {
        String name = upgrade.name();
        if (!UpgradeRegistry.getUpgrades().contains(upgrade)) {
            throw new IllegalArgumentException("Upgrade '" + name + "' is not registered");
        }
        if (!this.data.upgrades.settings.containsKey(name)) {
            // Shouldn't get here but let's be sure!
            throw new IllegalArgumentException("Upgrade '" + name + "' has no settings to enable or disable");
        }
        if (enabled) {
            this.data.upgrades.disabled.remove(name);
        } else if (!this.data.upgrades.disabled.contains(name)) {
            this.data.upgrades.disabled.add(name);
        }
    }

    @Override
    public void upgradeSettings(UpgradeSettings settings) {
        Upgrade upgrade = settings.upgrade();
        String name = upgrade.name();
        if (!UpgradeRegistry.getUpgrades().contains(upgrade)) {
            throw new IllegalArgumentException("Upgrade '" + name + "' is not registered");
        }
        if (settings.findFlaw() instanceof String issue) {
            throw new IllegalArgumentException(issue);
        }
        this.data.upgrades.settings.put(name, settings);
    }

    public abstract void forceSave(boolean sync);

    public void load() {
        this.loadData();

        // Setup or cleanup bad set time
        LocalTime lastChecked = this.data.shields.lastRunShieldCheck;
        this.data.shields.lastRunShieldCheck = MiscUtil.floorToHalfHour(lastChecked == null ? LocalTime.now() : lastChecked);

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
                                    Upgrades.Variables.COUNT, LeveledValueProvider.LevelMap.of(BigDecimal.valueOf(Confs.main().commands().warp().getMaxWarps()))
                            ),
                            1,
                            1,
                            LeveledValueProvider.LevelMap.of(BigDecimal.ZERO)
                    );
                }
                if (upgrade.upgrade() == Upgrades.TNT_BANK) {
                    if (Transitioner.migrateTNT()) {
                        upgrade = new UpgradeSettings(
                                Upgrades.TNT_BANK,
                                Map.of(Upgrades.Variables.COUNT, LeveledValueProvider.LevelMap.of(BigDecimal.valueOf(Transitioner.migrateTNTMax()))),
                                1,
                                1,
                                LeveledValueProvider.LevelMap.of(BigDecimal.valueOf(10000))
                        );
                    }
                }

                this.data.upgrades.settings.put(name, upgrade);

                if (
                        !( // Negate the conditions for if-should-default-enable, which my brain finds easier to read.
                                (upgrade.upgrade() == Upgrades.FLIGHT && Confs.main().commands().fly().isEnable()) ||
                                        (upgrade.upgrade() == Upgrades.WARPS && Confs.main().commands().warp().getMaxWarps() > 0) ||
                                        (upgrade.upgrade() == Upgrades.TNT_BANK && Transitioner.migrateTNT())
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
