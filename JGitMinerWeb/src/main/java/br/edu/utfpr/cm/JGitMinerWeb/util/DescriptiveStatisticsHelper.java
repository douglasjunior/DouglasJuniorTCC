package br.edu.utfpr.cm.JGitMinerWeb.util;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class DescriptiveStatisticsHelper extends DescriptiveStatistics {
    private static final double QUANTILE = new Median().getQuantile();

    public DescriptiveStatisticsHelper() {
    }

    public DescriptiveStatisticsHelper(int window) throws MathIllegalArgumentException {
        super(window);
    }

    public DescriptiveStatisticsHelper(double[] initialDoubleArray) {
        super(initialDoubleArray);
    }

    public DescriptiveStatisticsHelper(DescriptiveStatistics original) throws NullArgumentException {
        super(original);
    }

    @Override
    public void addValue(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            super.addValue(0);
        } else {
            super.addValue(v);
        }
    }

    @Override
    public double getMax() {
        double max = super.getMax();
        return Double.isNaN(max) ? 0.0d : max;
    }

    @Override
    public double getMean() {
        double mean = super.getMean();
        return Double.isNaN(mean) ? 0.0d : mean;
    }

    @Override
    public double getSum() {
        double sum = super.getSum();
        return Double.isNaN(sum) ? 0.0d : sum;
    }

    public double getMedian() {
        double median = super.getPercentile(QUANTILE);
        return Double.isNaN(median) ? 0.0d : median;
    }
}
