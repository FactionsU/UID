package dev.kitteh.factions.data;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.upgrade.Upgrade;
import dev.kitteh.factions.upgrade.UpgradeSettings;
import dev.kitteh.factions.upgrade.Upgrades;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Duration getGraceRemaining() {
        long currentTime = System.currentTimeMillis();
        if (this.data.grace.graceTimeEnd < currentTime) {
            this.data.grace.graceTimeEnd = 0L;
        }
        return this.data.grace.graceTimeEnd == 0L ? Duration.ZERO : Duration.ofMillis(this.data.grace.graceTimeEnd - currentTime);
    }

    @Override
    public void setGraceRemaining(Duration graceRemaining) {
        this.data.grace.graceTimeEnd = graceRemaining.isZero() ? 0L : System.currentTimeMillis() + graceRemaining.toMillis();
    }

    @Override
    public boolean isUpgradeEnabled(Upgrade upgrade) {
        return !this.data.upgrades.disabled.contains(upgrade.name());
    }

    @Override
    public UpgradeSettings getUpgradeSettings(Upgrade upgrade) {
        return this.data.upgrades.settings.get(upgrade.name());
    }

    public abstract void forceSave(boolean sync);

    public void load() {
        this.loadData();
        Upgrades.defaults.forEach(upgrade -> {
            String name = upgrade.upgrade().name();
            if (!this.data.upgrades.settings.containsKey(name)) {
                this.data.upgrades.settings.put(name, upgrade);
                this.data.upgrades.disabled.add(name);
            }
        });
    }

    protected abstract void loadData();
}
