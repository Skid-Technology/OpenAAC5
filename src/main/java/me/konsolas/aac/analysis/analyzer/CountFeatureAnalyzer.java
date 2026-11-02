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

public class CountFeatureAnalyzer extends FeatureAnalyzer {

    private final FeatureType feature;

    private final DoubleUnaryOperator scoringFunction;

    public CountFeatureAnalyzer(String text, FeatureType feature, DoubleUnaryOperator scoringFunction) {
        super(text);
        this.feature = feature;
        this.scoringFunction = scoringFunction;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        LinkedHashMap entries = new LinkedHashMap();
        FeatureType feature2 = this.feature;
        int count = featureAccumulator.getCount(feature2);
        double value = Math.min(this.scoringFunction.applyAsDouble(count), 1000.0);
        entries.put("id", this.getConfigurationPath());
        entries.put("score", String.format(Locale.UK, "%.1f", value));
        entries.put("count", Integer.toString(count));
        return Optional.ofNullable(FeatureResult.create(text, entries, value));
    }
}
