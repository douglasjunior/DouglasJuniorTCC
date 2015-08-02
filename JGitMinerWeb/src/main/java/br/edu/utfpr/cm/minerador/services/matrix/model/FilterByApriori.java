package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Define limits for apriori values, such as support and confidence metrics.
 *
 * @author Rodrigo T. Kuroda
 */
public class FilterByApriori {

    private final Double minSupport;
    private final Double maxSupport;
    private final Double minConfidence;
    private final Double maxConfidence;
    // minimum issues where co-change (file pair) appears
    private final Integer minIssues;
    private final Integer maxIssues;

    /**
     * Creates a filter with zero as value for minimum support and confidence.
     *
     * @param minSupport
     * @param minConfidence
     */
    public FilterByApriori(Double minSupport, Double minConfidence) {
        this.minSupport = minSupport;
        this.maxSupport = null;
        this.minConfidence = minConfidence;
        this.maxConfidence = null;
        this.minIssues = null;
        this.maxIssues = null;
    }

    /**
     * Creates a filter with zero as value for minimum support and confidence.
     *
     * @param minSupport
     * @param minConfidence
     */
    public FilterByApriori(Double minSupport, Double minConfidence, Integer minIssues) {
        this.minSupport = minSupport;
        this.maxSupport = null;
        this.minConfidence = minConfidence;
        this.maxConfidence = null;
        this.minIssues = minIssues;
        this.maxIssues = null;
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
    public FilterByApriori(Double minSupport, Double maxSupport, Double minConfidence, Double maxConfidence, Integer minIssues, Integer maxIssues) {
        this.minSupport = minSupport;
        this.maxSupport = maxSupport;
        this.minConfidence = minConfidence;
        this.maxConfidence = maxConfidence;
        this.minIssues = minIssues;
        this.maxIssues = maxIssues;
    }

    public Double getMinSupport() {
        return minSupport;
    }

    public Double getMaxSupport() {
        return maxSupport;
    }

    public Double getMinConfidence() {
        return minConfidence;
    }

    public Double getMaxConfidence() {
        return maxConfidence;
    }

    public Integer getMinIssues() {
        return minIssues;
    }

    public Integer getMaxIssues() {
        return maxIssues;
    }

    public static Set<FilterByApriori> getSuggestedFilters() {
        Set<FilterByApriori> filters = new LinkedHashSet<>();
//        filters.add(new FilterByApriori(0.01d, 0.1d));
//        filters.add(new FilterByApriori(0.01d, 0.5d));
//        filters.add(new FilterByApriori(0.01d, 0.8d));
//        filters.add(new FilterByApriori(0.03d, 0.1d));
//        filters.add(new FilterByApriori(0.03d, 0.5d));
//        filters.add(new FilterByApriori(0.03d, 0.8d));
//        filters.add(new FilterByApriori(0.05d, 0.1d));
//        filters.add(new FilterByApriori(0.05d, 0.5d));
//        filters.add(new FilterByApriori(0.05d, 0.8d));

        // TODO
        filters.add(new FilterByApriori(null, 0.9d, 3));
        filters.add(new FilterByApriori(0.02d, 0.8d));
        return filters;
    }

    public static Set<FilterByApriori> getFiltersForExperiment1() {
        Set<FilterByApriori> filters = new LinkedHashSet<>();

        filters.add(new FilterByApriori(null, null, 0.5d, 0.7d, 2, 2));
        filters.add(new FilterByApriori(null, null, 0.7d, 0.9d, 2, 2));
        filters.add(new FilterByApriori(null, null, 0.9d, null, 2, 2));

        filters.add(new FilterByApriori(null, null, 0.5d, 0.7d, 3, 3));
        filters.add(new FilterByApriori(null, null, 0.7d, 0.9d, 3, 3));
        filters.add(new FilterByApriori(null, null, 0.9d, null, 3, 3));

        filters.add(new FilterByApriori(null, null, 0.5d, 0.7d, 4, 5));
        filters.add(new FilterByApriori(null, null, 0.7d, 0.9d, 4, 5));
        filters.add(new FilterByApriori(null, null, 0.9d, null, 4, 5));

        filters.add(new FilterByApriori(null, null, 0.5d, 0.7d, 6, 7));
        filters.add(new FilterByApriori(null, null, 0.7d, 0.9d, 6, 7));
        filters.add(new FilterByApriori(null, null, 0.9d, null, 6, 7));

        filters.add(new FilterByApriori(null, null, 0.5d, 0.7d, 8, null));
        filters.add(new FilterByApriori(null, null, 0.7d, 0.9d, 8, null));
        filters.add(new FilterByApriori(null, null, 0.9d, null, 8, null));

        return filters;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.minSupport);
        hash = 71 * hash + Objects.hashCode(this.maxSupport);
        hash = 71 * hash + Objects.hashCode(this.minConfidence);
        hash = 71 * hash + Objects.hashCode(this.maxConfidence);
        hash = 71 * hash + Objects.hashCode(this.minIssues);
        hash = 71 * hash + Objects.hashCode(this.maxIssues);
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
        if (!Objects.equals(this.minSupport, other.minSupport)) {
            return false;
        }
        if (!Objects.equals(this.maxSupport, other.maxSupport)) {
            return false;
        }
        if (!Objects.equals(this.minConfidence, other.minConfidence)) {
            return false;
        }
        if (!Objects.equals(this.maxConfidence, other.maxConfidence)) {
            return false;
        }
        if (!Objects.equals(this.minIssues, other.minIssues)) {
            return false;
        }
        if (!Objects.equals(this.maxIssues, other.maxIssues)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (minIssues != null && maxIssues != null) {
            if (Objects.equals(minIssues, maxIssues)) {
                sb.append("Issues = ").append(minIssues);
            } else {
                sb.append("Min issues ").append(minIssues)
                        .append(", Max issues ").append(maxIssues);
            }
        } else if (minIssues != null) {
            sb.append("Min issues ").append(minIssues);

        } else if (maxIssues != null) {
            sb.append("Max issues ").append(maxIssues);
        }

        if (minSupport != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Min support ").append(minSupport);
        }

        if (maxSupport != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Max support ").append(maxSupport);
        }

        if (minConfidence != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Min confidence ").append(minConfidence);
        }

        if (maxConfidence != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Max confidence ").append(maxConfidence);
        }

        return sb.toString();
    }

}
