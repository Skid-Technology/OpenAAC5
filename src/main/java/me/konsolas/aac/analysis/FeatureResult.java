package me.konsolas.aac.analysis;

import java.util.Map;
import java.util.Map.Entry;

public class FeatureResult {

    public static final double IGNORED_SCORE = 1000.0;

    private final RiskLevel riskLevel;

    private final String description;

    private final Map details;

    private final double score;

    public FeatureResult(RiskLevel riskLevel, String description, Map details, double score) {
        for (Entry entry : (Iterable<Entry>) (Iterable<?>) (details.entrySet())) {
            description = description.replace("{" + (String) entry.getKey() + "}", (CharSequence) entry.getValue());
        }
        this.riskLevel = riskLevel;
        this.description = description;
        this.details = details;
        this.score = score;
    }

    public static FeatureResult create(String text, Map entries, double value) {
        if (value >= 10.0) {
            RiskLevel riskLevel = value > 25.0 ? (value > 100.0 ? RiskLevel.HIGH : RiskLevel.SUSPICIOUS) : RiskLevel.NEUTRAL;
            double value2 = Math.min(value, 250.0);
            RiskLevel riskLevel2 = riskLevel;
            return new FeatureResult(riskLevel2, text, entries, value2);
        } else {
            return null;
        }
    }

    public FeatureResult scaledBy(double value) {
        double value2 = this.score * value;
        Map entries = this.details;
        String text = this.description;
        RiskLevel riskLevel2 = this.riskLevel;
        return new FeatureResult(riskLevel2, text, entries, value2);
    }

    public RiskLevel getRiskLevel() {
        return this.riskLevel;
    }

    public Map getDetails() {
        return this.details;
    }

    public String getDescription() {
        return this.description;
    }

    public double getScore() {
        return this.score;
    }
}
