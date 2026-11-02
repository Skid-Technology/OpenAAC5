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

public class FrequencyFeatureAnalyzer extends FeatureAnalyzer {

    private final FeatureType countFeature;

    private final FeatureType totalFeature;

    private final DoubleUnaryOperator scoringFunction;

    public FrequencyFeatureAnalyzer(String text, FeatureType countFeature, FeatureType totalFeature, DoubleUnaryOperator scoringFunction) {
        super(text);
        this.countFeature = countFeature;
        this.totalFeature = totalFeature;
        this.scoringFunction = scoringFunction;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        LinkedHashMap entries = new LinkedHashMap();
        FeatureType feature = this.totalFeature;
        double total = featureAccumulator.getTotal(feature);
        FeatureType feature2 = this.countFeature;
        int count = featureAccumulator.getCount(feature2);
        double value = count / Math.max(total, 0.5);
        double value2 = Math.min(this.scoringFunction.applyAsDouble(value), 1000.0);
        entries.put("id", this.getConfigurationPath());
        entries.put("score", String.format(Locale.UK, "%.1f", value2));
        entries.put("count", Integer.toString(count));
        entries.put("time", String.format(Locale.UK, "%.1f", total));
        entries.put("rate", String.format(Locale.UK, "%.1f", value));
        return Optional.ofNullable(FeatureResult.create(text, entries, value2));
    }
}
