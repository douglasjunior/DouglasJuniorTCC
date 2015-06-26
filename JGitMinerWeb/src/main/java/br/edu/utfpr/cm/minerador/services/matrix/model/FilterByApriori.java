package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Define limits for apriori values, such as support and confidence metrics.
 *
 * @author Rodrigo T. Kuroda
 */
public class FilterByApriori {

    private final double minSupport;
    private final double maxSupport;
    private final double minConfidence;
    private final double maxConfidence;

    /**
     * Creates a filter with zero as value for minimum support and confidence.
     *
     * @param maxSupport
     * @param maxConfidence
     */
    public FilterByApriori(double maxSupport, double maxConfidence) {
        this.minSupport = 0.0d;
        this.maxSupport = maxSupport;
        this.minConfidence = 0.0d;
        this.maxConfidence = maxConfidence;
    }

    /**
     * Creates a filter with corresponding parameters: minimum support, maximum
     * support, minimum confidence, and maximum confidence.
     *
     * @param minSupport
     * @param maxSupport
     * @param minConfidence
     * @param maxConfidence
     */
    public FilterByApriori(double minSupport, double maxSupport, double minConfidence, double maxConfidence) {
        this.minSupport = minSupport;
        this.maxSupport = maxSupport;
        this.minConfidence = minConfidence;
        this.maxConfidence = maxConfidence;
    }

    public double getMinSupport() {
        return minSupport;
    }

    public double getMaxSupport() {
        return maxSupport;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public double getMaxConfidence() {
        return maxConfidence;
    }

    public static Set<FilterByApriori> getSuggestedFilters() {
        Set<FilterByApriori> filters = new LinkedHashSet<>();
        filters.add(new FilterByApriori(0.01d, 0.1d));
        filters.add(new FilterByApriori(0.01d, 0.5d));
        filters.add(new FilterByApriori(0.01d, 0.8d));
        filters.add(new FilterByApriori(0.03d, 0.1d));
        filters.add(new FilterByApriori(0.03d, 0.5d));
        filters.add(new FilterByApriori(0.03d, 0.8d));
        filters.add(new FilterByApriori(0.05d, 0.1d));
        filters.add(new FilterByApriori(0.05d, 0.5d));
        filters.add(new FilterByApriori(0.05d, 0.8d));
        return filters;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.minSupport) ^ (Double.doubleToLongBits(this.minSupport) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.maxSupport) ^ (Double.doubleToLongBits(this.maxSupport) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.minConfidence) ^ (Double.doubleToLongBits(this.minConfidence) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.maxConfidence) ^ (Double.doubleToLongBits(this.maxConfidence) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilterByApriori other = (FilterByApriori) obj;
        if (Double.doubleToLongBits(this.minSupport) != Double.doubleToLongBits(other.minSupport)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxSupport) != Double.doubleToLongBits(other.maxSupport)) {
            return false;
        }
        if (Double.doubleToLongBits(this.minConfidence) != Double.doubleToLongBits(other.minConfidence)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxConfidence) != Double.doubleToLongBits(other.maxConfidence)) {
            return false;
        }
        return true;
    }

}
