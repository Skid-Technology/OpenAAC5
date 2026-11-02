package me.konsolas.aac.util.math;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

public class MinecraftMath {

    public static final float SQRT_TWO = sqrt(2.0F);

    private static final float[] SINE_TABLE = (float[]) initialize(new float[65536], result -> {
        for (int index = 0; index < result.length; index++) {
            result[index] = (float) Math.sin(index * Math.PI * 2.0 / 65536.0);
        }
    });

    private static final Random RANDOM = new Random();

    private static final int[] DE_BRUIJN_TABLE = new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9 };

    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);

    private static final double[] ASIN_TABLE = new double[257];

    private static final double[] COS_TABLE = new double[257];

    public static <T> T initialize(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static float sin(float value) {
        return SINE_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value) {
        return SINE_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqrt(double value) {
        return (float) Math.sqrt(value);
    }

    public static int floor(float value) {
        int value2 = (int) value;
        return value < value2 ? value2 - 1 : value2;
    }

    public static int fastFloor(double value) {
        return (int) (value + 1024.0) - 1024;
    }

    public static int floor(double value) {
        int value2 = (int) value;
        return value < value2 ? value2 - 1 : value2;
    }

    public static long lfloor(double value) {
        long value2 = (long) value;
        return value < value2 ? value2 - 1L : value2;
    }

    public static int absFloor(double value) {
        return (int) (value >= 0.0 ? value : -value + 1.0);
    }

    public static float abs(float value) {
        return value >= 0.0F ? value : -value;
    }

    public static int abs(int value) {
        return value >= 0 ? value : -value;
    }

    public static int ceil(float value) {
        int value2 = (int) value;
        return value > value2 ? value2 + 1 : value2;
    }

    public static int ceil(double value) {
        int value2 = (int) value;
        return value > value2 ? value2 + 1 : value2;
    }

    public static int clamp(int value, int minimum, int maximum) {
        return value < minimum ? minimum : Math.min(value, maximum);
    }

    public static float clamp(float value, float minimum, float maximum) {
        return value < minimum ? minimum : Math.min(value, maximum);
    }

    public static double clamp(double value, double minimum, double maximum) {
        return value < minimum ? minimum : Math.min(value, maximum);
    }

    public static double clampedLerp(double value, double value2, double value3) {
        if (value3 < 0.0) {
            return value;
        } else {
            return value3 > 1.0 ? value2 : value + (value2 - value) * value3;
        }
    }

    public static double absMax(double value, double value2) {
        if (value < 0.0) {
            value = -value;
        }
        if (value2 < 0.0) {
            value2 = -value2;
        }
        return Math.max(value, value2);
    }

    public static int floorDiv(int value, int value2) {
        return Math.floorDiv(value, value2);
    }

    public static int nextInt(Random random, int value, int value2) {
        return value >= value2 ? value : random.nextInt(value2 - value + 1) + value;
    }

    public static float nextFloat(Random random, float value, float value2) {
        return value >= value2 ? value : random.nextFloat() * (value2 - value) + value;
    }

    public static double nextDouble(Random random, double value, double value2) {
        return value >= value2 ? value : random.nextDouble() * (value2 - value) + value;
    }

    public static double average(long[] values) {
        long value = 0L;
        for (long value2 : values) {
            value += value2;
        }
        return (double) value / values.length;
    }

    public static boolean approximatelyEqual(float value, float value2) {
        float value3 = value2 - value;
        return abs(value3) < 1.0E-5F;
    }

    public static int positiveModulo(int value, int modulus) {
        return Math.floorMod(value, modulus);
    }

    public static float positiveModulo(float value, float modulus) {
        return (value % modulus + modulus) % modulus;
    }

    public static double positiveModulo(double value, double modulus) {
        return (value % modulus + modulus) % modulus;
    }

    public static float wrapDegrees(float value) {
        value %= 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }
        if (value < -180.0F) {
            value += 360.0F;
        }
        return value;
    }

    public static double wrapDegrees(double value) {
        value %= 360.0;
        if (value >= 180.0) {
            value -= 360.0;
        }
        if (value < -180.0) {
            value += 360.0;
        }
        return value;
    }

    public static int wrapDegrees(int value) {
        value %= 360;
        if (value >= 180) {
            value -= 360;
        }
        if (value < -180) {
            value += 360;
        }
        return value;
    }

    public static float degreesDifference(float value, float value2) {
        float value3 = value - value2;
        float value4 = value3;
        float value5 = wrapDegrees(value4);
        return value5 < 180.0F ? value5 : value5 - 360.0F;
    }

    public static float degreesDifferenceAbs(float value, float value2) {
        float value4 = value - value2;
        float value5 = wrapDegrees(value4);
        float value6;
        if (value5 < 180.0F) {
            value6 = abs(value5);
        } else {
            float value7 = value5 - 360.0F;
            value6 = abs(value7);
        }
        return value6;
    }

    public static float approach(float current, float target, float step) {
        step = abs(step);
        float value4;
        if (current < target) {
            float value5 = current + step;
            float value6 = value5;
            value4 = clamp(value6, current, target);
        } else {
            float value7 = current - step;
            value4 = clamp(value7, target, current);
        }
        return value4;
    }

    public static float approachDegrees(float value, float value2, float value4) {
        float value5 = degreesDifference(value2, value);
        float value6 = value + value5;
        return approach(value, value6, value4);
    }

    public static double parseDouble(String text, double value) {
        try {
            return Double.parseDouble(text);
        } catch (Throwable throwable) {
            return value;
        }
    }

    public static double parseDouble(String text, double value, double value2) {
        return Math.max(value2, parseDouble(text, value));
    }

    public static int smallestEncompassingPowerOfTwo(int value) {
        int value2 = value - 1;
        value2 |= value2 >> 1;
        value2 |= value2 >> 2;
        value2 |= value2 >> 4;
        value2 |= value2 >> 8;
        value2 |= value2 >> 16;
        return value2 + 1;
    }

    private static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    public static int ceilLog2(int value) {
        value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
        return DE_BRUIJN_TABLE[(int) (value * 125613361L >> 27) & 31];
    }

    public static int log2(int value) {
        return ceilLog2(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static int roundUp(int value, int value2) {
        if (value2 == 0) {
            return 0;
        } else if (value == 0) {
            return value2;
        } else {
            if (value < 0) {
                value2 *= -1;
            }
            int value3 = value % value2;
            return value3 == 0 ? value : value + value2 - value3;
        }
    }

    public static int color(float value, float value2, float value3) {
        float value4 = value * 255.0F;
        int value5 = floor(value4);
        float value6 = value2 * 255.0F;
        int value7 = floor(value6);
        float value8 = value3 * 255.0F;
        return color(value5, value7, floor(value8));
    }

    public static int color(int value, int value2, int value3) {
        int value4 = (value << 8) + value2;
        return (value4 << 8) + value3;
    }

    public static int multiplyColor(int value, int value2) {
        int value3 = (value & 0xFF0000) >> 16;
        int value4 = (value2 & 0xFF0000) >> 16;
        int value5 = (value & 0xFF00) >> 8;
        int value6 = (value2 & 0xFF00) >> 8;
        int value7 = value & 0xFF;
        int value8 = value2 & 0xFF;
        int value9 = (int) ((float) value3 * value4 / 255.0F);
        int value10 = (int) ((float) value5 * value6 / 255.0F);
        int value11 = (int) ((float) value7 * value8 / 255.0F);
        return value & 0xFF000000 | value9 << 16 | value10 << 8 | value11;
    }

    public static double fraction(double value) {
        return value - Math.floor(value);
    }

    public static long coordinateSeed(int value, int value2, int value3) {
        long value4 = value * 3129871L ^ value3 * 116129781L ^ value2;
        value4 = value4 * value4 * 42317861L + value4 * 11L;
        return value4 >> 16;
    }

    public static UUID randomUuid(Random random) {
        long value = random.nextLong() & -61441L | 16384L;
        long value2 = random.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(value, value2);
    }

    public static UUID randomUuid() {
        return randomUuid(RANDOM);
    }

    public static double inverseLerp(double value, double value2, double value3) {
        return (value - value2) / (value3 - value2);
    }

    public static double atan2(double y, double x) {
        double lengthSquared = x * x + y * y;
        if (Double.isNaN(lengthSquared))
            return Double.NaN;
        boolean negativeY = y < 0.0;
        if (negativeY)
            y = -y;
        boolean negativeX = x < 0.0;
        if (negativeX)
            x = -x;
        boolean swap = y > x;
        if (swap) {
            double temporary = x;
            x = y;
            y = temporary;
        }
        double inverse = fastInverseSqrt(lengthSquared);
        x *= inverse;
        y *= inverse;
        double biased = FRAC_BIAS + y;
        int index = (int) Double.doubleToRawLongBits(biased);
        double angle = ASIN_TABLE[index];
        double cosine = COS_TABLE[index];
        double roundedY = biased - FRAC_BIAS;
        double error = y * cosine - x * roundedY;
        angle += (6.0 + error * error) * error * 0.16666666666666666;
        if (swap)
            angle = Math.PI / 2 - angle;
        if (negativeX)
            angle = Math.PI - angle;
        if (negativeY)
            angle = -angle;
        return angle;
    }

    public static double fastInverseSqrt(double value) {
        double value2 = 0.5 * value;
        long value3 = Double.doubleToRawLongBits(value);
        value3 = 6910469410427058090L - (value3 >> 1);
        value = Double.longBitsToDouble(value3);
        return value * (1.5 - value2 * value * value);
    }

    public static int murmurHash3(int value) {
        value ^= value >>> 16;
        value *= -2048144789;
        value ^= value >>> 13;
        value *= -1028477387;
        return value ^ value >>> 16;
    }

    public static int binarySearch(int value, int value2, IntPredicate intPredicate) {
        int value3 = value2 - value;
        while (value3 > 0) {
            int value4 = value3 / 2;
            int value5 = value + value4;
            if (intPredicate.test(value5)) {
                value3 = value4;
            } else {
                value = value5 + 1;
                value3 -= value4 + 1;
            }
        }
        return value;
    }

    static {
        for (int index = 0; index < 257; index++) {
            double value = index / 256.0;
            double value2 = Math.asin(value);
            COS_TABLE[index] = Math.cos(value2);
            ASIN_TABLE[index] = value2;
        }
    }
}
