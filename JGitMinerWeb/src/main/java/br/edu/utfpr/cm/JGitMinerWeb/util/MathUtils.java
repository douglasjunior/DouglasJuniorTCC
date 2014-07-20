
package br.edu.utfpr.cm.JGitMinerWeb.util;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class MathUtils {

    /**
     * Calcule weighted geometric average. Needs a matrix with values and
     * weight, where each matrix line (vector) contains: 0 = value 1 = weight
     *
     * @param valuesAndWeight
     * @return weighted geometric average
     */
    public static double calculateWeightedGeometricAverage(long[][] valuesAndWeight) {
        long weightSum = 0;
        double weightedValues = 1;
        for (long[] weightAndvalue : valuesAndWeight) {
            long value = weightAndvalue[0];
            long weight = weightAndvalue[1];
            weightSum += weight;
            weightedValues *= Math.pow(value, weight);
        }
        return Math.pow(weightedValues, (1.0d / weightSum));
    }
}
