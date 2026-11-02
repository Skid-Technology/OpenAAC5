package me.konsolas.aac.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class MouseSensitivityMath {

    public static final double FLOAT_PRECISION = Math.pow(2.0, 24.0);

    public static int sensitivityPercent(double value) {
        return BigDecimal.valueOf(value / 0.5 * 100.0).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public static double mode(double[] values2) {
        Map<Double, Long> entries = DoubleStream.of(values2).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long value = entries.values().stream().max(Comparator.naturalOrder()).orElseThrow(IllegalArgumentException::new);
        return entries.entrySet().stream().filter(result -> (Long) result.getValue() == value).map(Entry::getKey).min(Comparator.naturalOrder()).orElseThrow(IllegalArgumentException::new);
    }

    public static double estimateSensitivity(double value) {
        double value2 = rawMouseDelta(value) / 8.0;
        double value3 = Math.cbrt(value2);
        double value4 = value3 - 0.2F;
        return value4 / 0.6F;
    }

    public static double estimateAbsoluteSensitivity(double value) {
        double value2 = absoluteRawMouseDelta(value) / 8.0;
        double value3 = Math.cbrt(value2);
        double value4 = value3 - 0.2F;
        return value4 / 0.6F;
    }

    public static double sensitivityStep(double value) {
        double value2 = sensitivityFactor(value);
        return Math.pow(value2, 3.0) * 8.0;
    }

    public static double sensitivityFactor(double value) {
        double value2 = estimateSensitivity(value);
        return value2 * 0.6F + 0.2;
    }

    public static double absoluteSensitivityFactor(double value) {
        double value2 = estimateAbsoluteSensitivity(value);
        return value2 * 0.6F + 0.2;
    }

    public static double absoluteSensitivityStep(double value) {
        double value2 = absoluteSensitivityFactor(value);
        return (float) Math.pow(value2, 3.0) * 8.0F;
    }

    public static double rawMouseDelta(double value) {
        return value / 0.15;
    }

    public static int mouseCounts(double value, double value2) {
        double value3 = rawMouseDelta(value);
        return Math.round((float) (value3 / sensitivityStep(value2)));
    }

    public static int absoluteMouseCounts(double value, double value2) {
        double value3 = absoluteRawMouseDelta(value);
        return Math.round((float) (value3 / absoluteSensitivityStep(value2)));
    }

    public static double absoluteRawMouseDelta(double value) {
        int value2 = value >= 0.0 ? 1 : -1;
        return value / 0.15 / value2;
    }

    public static int floor(double value) {
        int value2 = (int) value;
        return value < value2 ? value2 - 1 : value2;
    }

    public static long approximateGcd(long value, long value2) {
        long value3;
        if (value2 <= 16384L) {
            value3 = value;
        } else {
            long value4 = value % value2;
            value3 = approximateGcd(value2, value4);
        }
        return value3;
    }
}
