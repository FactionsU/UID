package dev.kitteh.factions.upgrade;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.MiscUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.function.Function;

/**
 * An upgrade variable
 *
 * @param name name
 * @param min minimum value
 * @param max maximum value
 * @param formatter string formatter for the information
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public record UpgradeVariable(String name, BigDecimal min, BigDecimal max, Function<BigDecimal, String> formatter) {
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    /**
     * Helper to create a variable that just outputs the value as a plain string.
     *
     * @param name name
     * @param min minimum value
     * @param max maximum value
     * @return variable with these settings
     */
    public static UpgradeVariable of(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, BigDecimal::toPlainString);
    }

    /**
     * Helper to create a variable that always outputs as an integer value.
     *
     * @param name name
     * @param min minimum value
     * @param max maximum value
     * @return variable with these settings
     */
    public static UpgradeVariable ofInteger(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, b -> {
            BigInteger bigInteger = b.toBigInteger();
            if (bigInteger.equals(INT_MAX)) {
                return FactionsPlugin.instance().tl().upgrades().getUnlimited();
            }
            return b.toBigInteger().toString();
        });
    }

    /**
     * Helper to create a variable that outputs as a percent.
     *
     * @param name name
     * @param min minimum value
     * @param max maximum value
     * @return variable with these settings
     */
    public static UpgradeVariable ofPercent(String name, BigDecimal min, BigDecimal max) {
        return new UpgradeVariable(name, min, max, b -> b.movePointRight(2).setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    /**
     * Helper to create a variable tracking a duration.
     *
     * @param name name
     * @return variable with these settings
     */
    public static UpgradeVariable ofDuration(String name) {
        return new UpgradeVariable(name, BigDecimal.ONE, BigDecimal.valueOf(Integer.MAX_VALUE), b -> {
            BigInteger bigInteger = b.toBigInteger();
            Duration duration = Duration.ofSeconds(bigInteger.longValue());

            return MiscUtil.durationString(duration);
        });
    }

    /**
     * Clamps an input value to the min and max values the variable can have.
     *
     * @param value input value
     * @return clamped value
     */
    public BigDecimal get(BigDecimal value) {
        return value.max(min).min(max);
    }
}
