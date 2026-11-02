package me.konsolas.aac.analysis.analyzer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.konsolas.aac.analysis.FeatureResult;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.analysis.statistics.StatisticalAnalysis;
import me.konsolas.aac.analysis.statistics.StatisticalTestResult;
import me.konsolas.aac.player.PlayerLoginInfo;

public class TTestFeatureAnalyzer extends FeatureAnalyzer {

    private final FeatureType feature;

    private final double expectedMean;

    private final boolean upperTail;

    public TTestFeatureAnalyzer(String text, FeatureType feature, double expectedMean, boolean upperTail) {
        super(text);
        this.feature = feature;
        this.expectedMean = expectedMean;
        this.upperTail = upperTail;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        boolean matches = this.upperTail;
        double value = this.expectedMean;
        FeatureType feature2 = this.feature;
        Optional optional = StatisticalAnalysis.testMean(feature2, values, value, matches);
        if (optional.isPresent()) {
            double value2 = Math.min(1.0 / ((StatisticalTestResult) optional.get()).getPValue(), 1000.0);
            LinkedHashMap entries = new LinkedHashMap();
            entries.put("id", this.getConfigurationPath());
            entries.put("score", String.format(Locale.UK, "%.1f", value2));
            entries.put("count", Integer.toString(((StatisticalTestResult) optional.get()).getSampleCount()));
            entries.put("outliers", Integer.toString(((StatisticalTestResult) optional.get()).getOutlierCount()));
            entries.put("mean", String.format(Locale.UK, "%.5f", ((StatisticalTestResult) optional.get()).getMean()));
            entries.put("p", String.format(Locale.UK, "%.5f", ((StatisticalTestResult) optional.get()).getPValue()));
            entries.put("test", "Student's t-test");
            return Optional.ofNullable(FeatureResult.create(text, entries, value2));
        } else {
            return Optional.empty();
        }
    }
}
