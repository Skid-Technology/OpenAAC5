package me.konsolas.aac.analysis.analyzer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import me.konsolas.aac.analysis.FeatureResult;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.analysis.feature.FeatureType;
import me.konsolas.aac.player.PlayerLoginInfo;

public class AverageFeatureAnalyzer extends FeatureAnalyzer {

    private final FeatureType feature;

    private final DoubleUnaryOperator scoringFunction;

    public AverageFeatureAnalyzer(String text, FeatureType feature, DoubleUnaryOperator scoringFunction) {
        super(text);
        this.feature = feature;
        this.scoringFunction = scoringFunction;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        double value = 0.0;
        int value2 = 0;
        for (FeatureEvent event : values) {
            if (event.getType() == this.feature) {
                double value3 = event.getValue() - value;
                value2++;
                value += value3 / value2;
            }
        }
        if (value2 < 2) {
            return Optional.empty();
        } else {
            double value4 = Math.min(this.scoringFunction.applyAsDouble(value), 1000.0);
            LinkedHashMap entries = new LinkedHashMap();
            entries.put("id", this.getConfigurationPath());
            entries.put("score", String.format(Locale.UK, "%.1f", value4));
            entries.put("mean", String.format(Locale.UK, "%.4f", value));
            entries.put("count", Integer.toString(value2));
            return Optional.ofNullable(FeatureResult.create(text, entries, value4));
        }
    }
}
