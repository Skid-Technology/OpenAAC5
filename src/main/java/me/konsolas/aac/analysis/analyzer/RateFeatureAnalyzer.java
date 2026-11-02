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

public class RateFeatureAnalyzer extends FeatureAnalyzer {

    private final FeatureType countFeature;

    private final FeatureType totalFeature;

    private final DoubleUnaryOperator scoringFunction;

    public RateFeatureAnalyzer(String text, FeatureType countFeature, FeatureType totalFeature, DoubleUnaryOperator scoringFunction) {
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
        double total2 = featureAccumulator.getTotal(feature2);
        double value2 = total2 / Math.max(total, 0.5);
        double value3 = Math.min(this.scoringFunction.applyAsDouble(value2), 1000.0);
        entries.put("id", this.getConfigurationPath());
        entries.put("score", String.format(Locale.UK, "%.1f", value3));
        entries.put("total", String.format(Locale.UK, "%.1f", total2));
        entries.put("time", String.format(Locale.UK, "%.1f", total));
        entries.put("rate", String.format(Locale.UK, "%.1f", value2));
        return Optional.ofNullable(FeatureResult.create(text, entries, value3));
    }
}
