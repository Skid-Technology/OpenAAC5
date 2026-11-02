package me.konsolas.aac.analysis.analyzer;

import java.util.List;
import java.util.Optional;
import me.konsolas.aac.analysis.FeatureResult;
import me.konsolas.aac.analysis.feature.FeatureAccumulator;
import me.konsolas.aac.analysis.feature.FeatureEvent;
import me.konsolas.aac.player.PlayerLoginInfo;

public abstract class FeatureAnalyzer {

    public static final double weight = 0.5;

    private final String configurationPath;

    public FeatureAnalyzer(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public abstract Optional<FeatureResult> analyze(List<FeatureEvent> values, FeatureAccumulator featureAccumulator, PlayerLoginInfo playerLoginInfo, String text);

    public String getConfigurationPath() {
        return this.configurationPath;
    }
}
