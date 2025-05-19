package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.FactionsPlugin;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.function.Function;

@NullMarked
public record UpgradeVariable(String name, BigDecimal min, BigDecimal max, Function<BigDecimal, String> formatter) {
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    public static UpgradeVariable of(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, BigDecimal::toPlainString);
    }

    public static UpgradeVariable ofInteger(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, b -> {
            BigInteger bigInteger = b.toBigInteger();
            if (bigInteger.equals(INT_MAX)) {
                return FactionsPlugin.instance().tl().upgrades().getUnlimited();
            }
            return b.toBigInteger().toString();
        });
    }

    public static UpgradeVariable ofPercent(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, b -> b.movePointRight(2).setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    public BigDecimal get(BigDecimal value) {
        return value.max(min).min(max);
    }
}
