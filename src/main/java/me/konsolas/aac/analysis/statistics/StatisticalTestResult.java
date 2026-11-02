package me.konsolas.aac.analysis.statistics;

public class StatisticalTestResult {

    private final double pValue;

    private final int sampleCount;

    private final int outlierCount;

    private final double mean;

    public StatisticalTestResult(double pValue, int sampleCount, int outlierCount, double mean) {
        this.pValue = pValue;
        this.sampleCount = sampleCount;
        this.outlierCount = outlierCount;
        this.mean = mean;
    }

    public double getPValue() {
        return this.pValue;
    }

    public int getSampleCount() {
        return this.sampleCount;
    }

    public int getOutlierCount() {
        return this.outlierCount;
    }

    public double getMean() {
        return this.mean;
    }
}
