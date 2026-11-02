package me.konsolas.aac.util.math;

import org.hipparchus.optim.MaxEval;
import org.hipparchus.optim.OptimizationData;
import org.hipparchus.optim.nonlinear.scalar.GoalType;
import org.hipparchus.optim.univariate.BrentOptimizer;
import org.hipparchus.optim.univariate.SearchInterval;
import org.hipparchus.optim.univariate.UnivariateObjectiveFunction;
import org.hipparchus.optim.univariate.UnivariatePointValuePair;

public class MouseStepEstimator {

    private static final BrentOptimizer OPTIMIZER;

    private static final MaxEval MAX_EVALUATIONS;

    private static int gcd(int value, int value2) {
        while (value2 != 0) {
            int value3 = value;
            value = value2;
            value2 = value3 % value2;
        }
        return value;
    }

    private static double approximateGcd(double value, double value2) {
        while (Math.abs(value2) > 1.0E-4) {
            double value3 = value;
            value = value2;
            value2 = value3 % value2;
        }
        return value;
    }

    public static double estimateStep(double[] values) {
        double value = 0.0;
        double value2 = Double.POSITIVE_INFINITY;
        for (int index = 0; index < values.length; index++) {
            for (int value3 = index + 1; value3 < values.length; value3++) {
                double value4 = values[index];
                double value5 = values[value3];
                double value6 = value4;
                double value7 = approximateGcd(value6, value5);
                if (value7 > 0.00938562) {
                    double value8 = sumError(value7, values);
                    if (value8 < value2) {
                        value = value7;
                        value2 = value8;
                    }
                }
            }
        }
        for (int index2 = 1; index2 < 64; index2++) {
            double value9 = value / index2;
            if (value9 < 0.00938562) {
                break;
            }
            double value10 = sumError(value9, values);
            if (value10 < value2) {
                value = value9;
                value2 = value10;
            }
        }
        if (!(value <= 0.00938562) && !(value >= 0.62)) {
            UnivariateObjectiveFunction univariateObjectiveFunction = new UnivariateObjectiveFunction(new MouseStepError(values));
            BrentOptimizer brentOptimizer = OPTIMIZER;
            OptimizationData[] optimizationDatas = new OptimizationData[] { univariateObjectiveFunction, GoalType.MINIMIZE, MAX_EVALUATIONS, null };
            optimizationDatas[3] = new SearchInterval((double) 0.00938562, (double) 0.62, value);
            OptimizationData[] optimizationDatas2 = optimizationDatas;
            UnivariatePointValuePair univariatePointValuePair = brentOptimizer.optimize(optimizationDatas2);
            if (univariatePointValuePair.getValue() < value2) {
                value = univariatePointValuePair.getPoint();
            }
            int value11 = 0;
            for (double value12 : values) {
                int value13 = (int) Math.round(value12 / value);
                value11 = gcd(value13, value11);
            }
            return value * value11;
        } else {
            return 0.0;
        }
    }

    private static double sumError(double value, double[] values) {
        double value2 = 0.0;
        for (double value3 : values) {
            double value4 = value3 / value;
            double value5 = value4 - Math.round(value4);
            value2 += value5 * value5;
        }
        return value2;
    }

    static double evaluateError(double value, double[] values) {
        return sumError(value, values);
    }

    static {
        OPTIMIZER = new BrentOptimizer((double) 1.0E-10, (double) 1.0E-30);
        MAX_EVALUATIONS = new MaxEval((byte) 100);
    }
}
