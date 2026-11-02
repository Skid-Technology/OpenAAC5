package me.konsolas.aac.analysis.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import org.hipparchus.stat.StatUtils;
import org.hipparchus.stat.inference.TTest;

public class StatisticalAnalysis {

    private static final TTest T_TEST = new TTest();

    public static Optional<StatisticalTestResult> testMean(FeatureType feature, List<FeatureEvent> values, double value, boolean matches) {
        ArrayList<Double> values2 = new ArrayList();
        for (FeatureEvent event : values) {
            if (event.getType() == feature) {
                values2.add((double) event.getValue());
            }
        }
        int size2 = values2.size();
        if (values2.size() <= 6) {
            return Optional.empty();
        } else {
            int value2 = DistributionAnalysis.calculateCount(values2, (double) 0.05);
            double[] values3 = values2.stream().mapToDouble(Double::doubleValue).toArray();
            double value3 = StatUtils.mean(values3);
            if (matches) {
                if (value3 <= value) {
                    return Optional.empty();
                }
            } else if (value3 >= value) {
                return Optional.empty();
            }
            return Optional.of(new StatisticalTestResult(T_TEST.tTest(value, values3) / 2.0, size2, value2, value3));
        }
    }
}
