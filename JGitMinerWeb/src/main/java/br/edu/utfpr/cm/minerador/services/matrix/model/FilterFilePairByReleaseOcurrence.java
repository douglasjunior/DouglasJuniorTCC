package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilterFilePairByReleaseOcurrence {

    private final Integer minQuantity;
    private final Integer maxQuantity;

    public FilterFilePairByReleaseOcurrence(int minQuantity) {
        this.minQuantity = minQuantity;
        this.maxQuantity = null;
    }

    public FilterFilePairByReleaseOcurrence(int minQuantity, int maxQuantity) {
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public boolean fits(FilePairReleasesOccurenceCounter counter) {
        final int releasesOcurrences = counter.getReleasesOcurrences();
        if (minQuantity != null && minQuantity.equals(maxQuantity)) {
            if (minQuantity.equals(releasesOcurrences)) {
                return true;
            }
        } else {
            if (minQuantity != null && maxQuantity != null) {
                if (minQuantity <= releasesOcurrences
                        && maxQuantity >= releasesOcurrences) {
                    return true;
                }

            } else if (minQuantity != null) {
                if (minQuantity <= releasesOcurrences) {
                    return true;
                }

            } else if (maxQuantity != null) {
                if (maxQuantity >= releasesOcurrences) {
                    return true;
                }

            }
        }
        return false;
    }

    public static List<FilterFilePairByReleaseOcurrence> getSuggestedFilters() {
        return Arrays.asList(new FilterFilePairByReleaseOcurrence[]{
            new FilterFilePairByReleaseOcurrence(2, 2),
            new FilterFilePairByReleaseOcurrence(3, 3),
            new FilterFilePairByReleaseOcurrence(4, 4),
            new FilterFilePairByReleaseOcurrence(5)
                });
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.minQuantity);
        hash = 13 * hash + Objects.hashCode(this.maxQuantity);
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
        final FilterFilePairByReleaseOcurrence other = (FilterFilePairByReleaseOcurrence) obj;
        if (!Objects.equals(this.minQuantity, other.minQuantity)) {
            return false;
        }
        if (!Objects.equals(this.maxQuantity, other.maxQuantity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (minQuantity != null && maxQuantity != null) {
            if (minQuantity.equals(maxQuantity)) {
                return "in " + minQuantity + " releases";
            } else {
                return "in " + minQuantity + "-" + maxQuantity + " releases";
            }
        } else if (minQuantity != null) {
            return ">= " + minQuantity + " releases";
        } else if (maxQuantity != null) {
            return "<= " + maxQuantity + " releases";
        }

        return "";
    }

}
