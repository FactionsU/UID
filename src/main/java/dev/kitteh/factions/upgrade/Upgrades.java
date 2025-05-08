package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.config.file.TranslationsConfig;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@NullMarked
public final class Upgrades {
    public static final class Variables {
        public static final UpgradeVariable CHANCE = UpgradeVariable.ofPercent("chance", BigDecimal.ZERO, BigDecimal.valueOf(100));

        public static final UpgradeVariable GROWTH_BOOST = UpgradeVariable.ofInteger("boost", BigDecimal.ONE, BigDecimal.valueOf(100));

        public static final UpgradeVariable POSITIVE_INCREASE = UpgradeVariable.ofInteger("increase", BigDecimal.ONE, BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    public static final Upgrade DTR_CLAIM_LIMIT = new Upgrade.Simple("dtr_claim_limit", TranslationsConfig.Upgrades::dtrClaimLimit, Integer.MAX_VALUE, Set.of(Variables.POSITIVE_INCREASE));

    public static final Upgrade GROWTH = new Upgrade.Simple("growth", TranslationsConfig.Upgrades::growth, Integer.MAX_VALUE, Set.of(Variables.CHANCE, Variables.GROWTH_BOOST));

    public static final Upgrade MAX_MEMBERS = new Upgrade.Simple("max_members", TranslationsConfig.Upgrades::maxMembers, Integer.MAX_VALUE, Set.of(Variables.POSITIVE_INCREASE));

    public static final Upgrade ZONES = new Upgrade.Reactive("zones", TranslationsConfig.Upgrades::zones, Integer.MAX_VALUE, Set.of(Variables.POSITIVE_INCREASE),
            (f, o, n) -> {
                if (o == 0) {
                    f.getOnlinePlayers().forEach(Player::updateCommands);
                }
            });

    static final List<Upgrade> UPGRADES = new ArrayList<>();
    static final List<UpgradeVariable> VARIABLES = new ArrayList<>();

    static {
        for (Field field : Upgrades.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == Upgrade.class) {
                try {
                    UPGRADES.add((Upgrade) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (Field field : Upgrades.Variables.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == UpgradeVariable.class) {
                try {
                    Upgrades.VARIABLES.add((UpgradeVariable) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static final List<UpgradeSettings> defaults = List.of(
            new UpgradeSettings(
                    Upgrades.DTR_CLAIM_LIMIT,
                    Map.of(Variables.POSITIVE_INCREASE, LeveledValueProvider.Equation.of("level * 9")),
                    10,
                    LeveledValueProvider.Equation.of("100000 * level ^ 2")
            ),
            new UpgradeSettings(
                    Upgrades.GROWTH,
                    Map.of(
                            Variables.CHANCE, LeveledValueProvider.LevelMap.of(1, BigDecimal.valueOf(0.1), 2, BigDecimal.valueOf(0.25), 3, BigDecimal.valueOf(0.25)),
                            Variables.GROWTH_BOOST, LeveledValueProvider.LevelMap.of(1, BigDecimal.ONE, 2, BigDecimal.ONE, 3, BigDecimal.TWO)
                    ),
                    3,
                    LeveledValueProvider.LevelMap.of(1, BigDecimal.valueOf(2000000), 2, BigDecimal.valueOf(5000000), 3, BigDecimal.valueOf(10000000))
            ),
            new UpgradeSettings(
                    Upgrades.MAX_MEMBERS,
                    Map.of(
                            Variables.POSITIVE_INCREASE, LeveledValueProvider.Equation.of("level * 5")
                    ),
                    4,
                    LeveledValueProvider.Equation.of("100000*(level + 1)")
            ),
            new UpgradeSettings(
                    Upgrades.ZONES,
                    Map.of(
                            Variables.POSITIVE_INCREASE, LeveledValueProvider.LevelMap.of(1, BigDecimal.valueOf(Integer.MAX_VALUE))
                    ),
                    1,
                    LeveledValueProvider.LevelMap.of(1, BigDecimal.valueOf(10000))
            )
    );
}
