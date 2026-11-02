package me.konsolas.aac.analysis.feature;

public class FeatureAccumulator {

    private static final int FEATURE_COUNT = FeatureType.values().length;

    private final int[] counts = new int[FEATURE_COUNT];

    private final double[] totals = new double[FEATURE_COUNT];

    public void record(FeatureType feature, float value) {
        this.counts[feature.ordinal()]++;
        this.totals[feature.ordinal()] += value;
    }

    public int getCount(FeatureType feature) {
        return this.counts[feature.ordinal()];
    }

    public double getTotal(FeatureType feature) {
        return this.totals[feature.ordinal()];
    }
}
