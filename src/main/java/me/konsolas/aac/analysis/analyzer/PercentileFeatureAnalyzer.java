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
import org.hipparchus.stat.descriptive.rank.Percentile;

public class PercentileFeatureAnalyzer extends FeatureAnalyzer {

    private static final Percentile PERCENTILE = new Percentile();

    private final FeatureType valueFeature;

    private final FeatureType totalFeature;

    private final double percentile;

    private final DoubleUnaryOperator scoringFunction;

    public PercentileFeatureAnalyzer(String text, FeatureType valueFeature, FeatureType totalFeature, double percentile, DoubleUnaryOperator scoringFunction) {
        super(text);
        this.valueFeature = valueFeature;
        this.totalFeature = totalFeature;
        this.percentile = percentile;
        this.scoringFunction = scoringFunction;
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        FeatureType feature = this.totalFeature;
        double total = featureAccumulator.getTotal(feature);
        FeatureType feature2 = this.valueFeature;
        double[] values2 = new double[featureAccumulator.getCount(feature2)];
        int value = 0;
        for (FeatureEvent event : values) {
            if (event.getType() == this.valueFeature) {
                values2[value++] = event.getValue();
            }
        }
        if (values2.length > 10) {
            double value2 = this.percentile;
            double value3 = PERCENTILE.evaluate(values2, value2);
            double value4 = Math.min(this.scoringFunction.applyAsDouble(value3) * Math.min(Math.cbrt(total / 0.5), 1.0), 1000.0);
            LinkedHashMap entries = new LinkedHashMap();
            entries.put("id", this.getConfigurationPath());
            entries.put("score", String.format(Locale.UK, "%.1f", value4));
            entries.put("percentile", String.format(Locale.UK, "%.1f", this.percentile));
            entries.put("value", String.format(Locale.UK, "%.4f", value3));
            entries.put("count", Integer.toString(values2.length));
            entries.put("time", String.format(Locale.UK, "%.1f", total));
            return Optional.ofNullable(FeatureResult.create(text, entries, value4));
        } else {
            return Optional.empty();
        }
    }
}
