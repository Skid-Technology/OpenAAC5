package me.konsolas.aac.util.math;

import org.hipparchus.analysis.UnivariateFunction;

class MouseStepError implements UnivariateFunction {

    private final double[] samples;

    MouseStepError(double[] samples) {
        this.samples = samples;
    }

    public double value(double value2) {
        double[] values = this.samples;
        return MouseStepEstimator.evaluateError(value2, values);
    }
}
