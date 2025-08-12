package dev.kitteh.factions.upgrade;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.stream.IntStream;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public interface LeveledValueProvider {
    record Equation(Expression expression) implements LeveledValueProvider {
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

    record LevelMap(Int2ObjectArrayMap<BigDecimal> levels) implements LeveledValueProvider {
        public static LevelMap of(int level1, BigDecimal val1) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            return new LevelMap(levels);
        }

        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            return new LevelMap(levels);
        }

        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2, int level3, BigDecimal val3) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            levels.put(level3, val3);
            return new LevelMap(levels);
        }

        public static LevelMap of(int level1, BigDecimal val1, int level2, BigDecimal val2, int level3, BigDecimal val3, int level4, BigDecimal val4) {
            Int2ObjectArrayMap<BigDecimal> levels = new Int2ObjectArrayMap<>();
            levels.put(level1, val1);
            levels.put(level2, val2);
            levels.put(level3, val3);
            levels.put(level4, val4);
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

    BigDecimal get(int level);

    default boolean supportsUpToLevel(int level) {
        return true;
    }
}
