package me.konsolas.aac.analysis.analyzer;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.ToLongFunction;
import me.konsolas.aac.analysis.FeatureResult;
import me.konsolas.aac.analysis.PlayerAnalysisReport;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.player.PlayerLoginInfo;

public class AccountFeatureAnalyzer extends FeatureAnalyzer {

    private final double threshold;

    private final ToLongFunction<PlayerLoginInfo> timestampExtractor;

    public AccountFeatureAnalyzer(String text, double threshold, ToLongFunction<PlayerLoginInfo> timestampExtractor) {
        super(text);
        this.threshold = threshold;
        this.timestampExtractor = timestampExtractor;
    }

    private static double calculateValue(double value) {
        return 2.0 / (Math.exp(value) + Math.exp(-value));
    }

    @Override
    public Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text) {
        long value = this.timestampExtractor.applyAsLong(playerLoginInfo);
        long value2 = System.currentTimeMillis() - value;
        double value3 = value2 / 8.64E7;
        double value4 = value3;
        double value5 = Math.min(this.threshold * calculateValue(value4), 1000.0);
        LinkedHashMap entries = new LinkedHashMap();
        entries.put("id", this.getConfigurationPath());
        entries.put("score", String.format(Locale.UK, "%.1f", value5));
        entries.put("timestamp", PlayerAnalysisReport.DATE_FORMAT.format(Instant.ofEpochMilli(value)));
        entries.put("timezone", PlayerAnalysisReport.DATE_FORMAT.getZone().getId());
        entries.put("address", playerLoginInfo.getAddress());
        return Optional.ofNullable(FeatureResult.create(text, entries, value5));
    }
}
