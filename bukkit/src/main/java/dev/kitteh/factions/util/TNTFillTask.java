package dev.kitteh.factions.util;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.upgrade.Upgrades;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TNTFillTask implements Runnable {
    @Override
    public void run() {
        if (!FactionsPlugin.instance().conf().commands().tnt().isEnable()) {
            return;
        }
        if (!Universe.universe().isUpgradeEnabled(Upgrades.TNT_BANK_FILL)) {
            return;
        }
        for (Faction faction : Factions.factions().all()) {
            int level = faction.upgradeLevel(Upgrades.TNT_BANK_FILL);
            if (level == 0) {
                continue;
            }
            int current = faction.tntBank();
            int max = faction.tntBankMax();
            if (current >= max) {
                continue;
            }
            int increase = Universe.universe().upgradeSettings(Upgrades.TNT_BANK_FILL).valueAt(Upgrades.Variables.POSITIVE_INCREASE, level).intValue();
            if (increase <= 0) {
                continue;
            }
            faction.tntBank((int) Math.min(max, (long) current + increase));
        }
    }
}
