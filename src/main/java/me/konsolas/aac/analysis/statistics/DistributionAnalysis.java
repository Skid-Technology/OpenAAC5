package me.konsolas.aac.analysis.statistics;

import java.util.List;
import org.hipparchus.distribution.continuous.TDistribution;

public class DistributionAnalysis {

    public static int calculateCount(List values, double value) {
        int value2 = 0;
        while (checkDistributionAnalysis(values, value)) {
            value2++;
        }
        return value2;
    }

    private static boolean checkDistributionAnalysis(List<Double> values, double value) {
        double value2 = 0.0;
        double value3 = 0.0;
        int size2 = values.size();
        if (size2 <= 6) {
            return false;
        } else {
            for (double value4 : values) {
                value2 += value4;
                value3 += value4 * value4;
            }
            double value5 = value2 / size2;
            double value6 = value3 / size2 - value5 * value5;
            double value7 = Math.sqrt(value6);
            double value8 = 0.0;
            int value9 = -1;
            for (int index = 0; index < values.size(); index++) {
                double value10 = (Double) values.get(index);
                double value11 = Math.abs(value5 - value10);
                if (value11 > value8) {
                    value8 = value11;
                    value9 = index;
                }
            }
            double value12 = value8 / value7;
            double value13 = size2 - 2.0;
            double value14 = value13;
            TDistribution tDistribution = new TDistribution(value14);
            double value15 = value / (2 * size2);
            double value16 = tDistribution.inverseCumulativeProbability(value15);
            double value17 = value16 * value16;
            double value18 = (size2 - 1) / Math.sqrt(size2) * Math.sqrt(value17 / (size2 - 2 + value17));
            if (value12 > value18) {
                values.remove(value9);
                return true;
            } else {
                return false;
            }
        }
    }
}
