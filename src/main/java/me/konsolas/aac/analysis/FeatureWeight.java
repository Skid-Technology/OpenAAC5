package me.konsolas.aac.analysis;

class FeatureWeight {

    private final String featureName;

    private final double weight;

    public FeatureWeight(String featureName, double weight) {
        this.featureName = featureName;
        this.weight = weight;
    }

    static String findString(FeatureWeight featureWeight) {
        return featureWeight.featureName;
    }

    static double calculateValue(FeatureWeight featureWeight) {
        return featureWeight.weight;
    }
}
