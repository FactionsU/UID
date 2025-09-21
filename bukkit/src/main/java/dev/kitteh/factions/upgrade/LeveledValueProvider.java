package dev.kitteh.factions.upgrade;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * Provider of per-level values for upgrade variables and costs.
 */
@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public sealed interface LeveledValueProvider permits LeveledValueProvider.Equation, LeveledValueProvider.LevelMap {
    /**
     * Expression-based calculator of value.
     *
     * @param expression expression to calculate the value, using 'level' as the level.
     */
    record Equation(Expression expression) implements LeveledValueProvider {
        /**
         * Creates a value provider using an expression.
         *
         * @param expression expression to calculate the value, using 'level' as the level.
         * @return new value provider
         */
        public static Equation of(String expression) {
            return new Equation(new Expression(expression));
        }

        @Override
        public BigDecimal get(int level) {
            try {
                return this.expression
                        .with("level", level)
                        .evaluate().getNumberValue();
            } catch (EvaluationException | ParseException e) {
                return BigDecimal.ZERO;
            }
        }
    }

    /**
     * Map-based value provider.
     *
     * @param levels levels available
     */
    record LevelMap(Int2ObjectArrayMap<BigDecimal> levels) implements LeveledValueProvider {
        @Deprecated(since = "4.2.2", forRemoval = true)
        public static LevelMap of(int level1, BigDecimal val1) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            return new LevelMap(levels);
        }

        /**
         * Helper to create a single-level provider.
         *
         * @param val1 value at level 1
         * @return new value provider
         */
        public static LevelMap of(BigDecimal val1) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(1, val1);
            return new LevelMap(levels);
        }

        @Deprecated(since = "4.2.2", forRemoval = true)
        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            return new LevelMap(levels);
        }

        /**
         * Helper to create a two-level provider.
         *
         * @param val1 value at level 1
         * @param val2 value at level 2
         * @return new value provider
         */
        public static LevelMap of(BigDecimal val1, BigDecimal val2) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(1, val1);
            levels.put(2, val2);
            return new LevelMap(levels);
        }

        @Deprecated(since = "4.2.2", forRemoval = true)
        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2, int level3, BigDecimal val3) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            levels.put(level3, val3);
            return new LevelMap(levels);
        }

        /**
         * Helper to create a three-level provider.
         *
         * @param val1 value at level 1
         * @param val2 value at level 2
         * @param val3 value at level 3
         * @return new value provider
         */
        public static LevelMap of(BigDecimal val1, BigDecimal val2, BigDecimal val3) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(1, val1);
            levels.put(2, val2);
            levels.put(3, val3);
            return new LevelMap(levels);
        }

        @Deprecated(since = "4.2.2", forRemoval = true)
        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2, int level3, BigDecimal val3, int level4, BigDecimal val4) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            levels.put(level3, val3);
            levels.put(level4, val4);
            return new LevelMap(levels);
        }

        /**
         * Helper to create a four-level provider.
         *
         * @param val1 value at level 1
         * @param val2 value at level 2
         * @param val3 value at level 3
         * @param val4 value at level 4
         * @return new value provider
         */
        public static LevelMap of(BigDecimal val1, BigDecimal val2, BigDecimal val3, BigDecimal val4) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(1, val1);
            levels.put(2, val2);
            levels.put(3, val3);
            levels.put(4, val4);
            return new LevelMap(levels);
        }

        @Override
        public BigDecimal get(int level) {
            return this.levels.get(level);
        }

        @Override
        public boolean supportsUpToLevel(int level) {
            return IntStream.range(1, level + 1).allMatch(this.levels::containsKey);
        }
    }

    /**
     * Gets the value at the given level.
     *
     * @param level level for which to get the value
     * @return value at the given level
     */
    BigDecimal get(int level);

    /**
     * Gets if the upgrade supports the given level (and all levels up to it).
     *
     * @param level level to test
     * @return true if this provider supports all levels up to and including the given level
     */
    default boolean supportsUpToLevel(int level) {
        return true;
    }
}
