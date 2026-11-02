package me.konsolas.aac.analysis.analyzer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.konsolas.aac.analysis.FeatureResult;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.player.PlayerLoginInfo;

public class ThresholdFeatureAnalyzer extends FeatureAnalyzer {

    private final double threshold;

    private final FeatureType feature;

    public ThresholdFeatureAnalyzer(String text, double threshold, FeatureType feature) {
        super(text);
        this.threshold = threshold;
        this.feature = feature;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        FeatureType feature2 = this.feature;
        double total = featureAccumulator.getTotal(feature2);
        if (total <= 0.5) {
            LinkedHashMap entries = new LinkedHashMap();
            entries.put("id", this.getConfigurationPath());
            entries.put("score", String.format(Locale.UK, "%.1f", this.threshold));
            entries.put("time", String.format(Locale.UK, "%.1f", total));
            double value = this.threshold;
            return Optional.ofNullable(FeatureResult.create(text, entries, value));
        } else {
            return Optional.empty();
        }
    }
}
