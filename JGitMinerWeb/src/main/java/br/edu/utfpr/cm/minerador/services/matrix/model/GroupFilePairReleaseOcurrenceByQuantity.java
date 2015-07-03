package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class GroupFilePairReleaseOcurrenceByQuantity {

    private final Integer minQuantity;
    private final Integer maxQuantity;

    public GroupFilePairReleaseOcurrenceByQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
        this.maxQuantity = null;
    }

    public GroupFilePairReleaseOcurrenceByQuantity(int minQuantity, int maxQuantity) {
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
        if (minQuantity.equals(maxQuantity)) {
            if (minQuantity.equals(counter.getReleasesOcurrences())) {
                return true;
            }
        } else {
            if (minQuantity != null && maxQuantity != null) {
                if (minQuantity <= counter.getReleasesOcurrences()
                        && maxQuantity >= counter.getReleasesOcurrences()) {
                    return true;
                }

            } else if (minQuantity != null) {
                if (minQuantity <= counter.getReleasesOcurrences()) {
                    return true;
                }

            } else if (maxQuantity != null) {
                if (maxQuantity >= counter.getReleasesOcurrences()) {
                    return true;
                }

            }
        }
        return false;
    }

    public static List<GroupFilePairReleaseOcurrenceByQuantity> getSuggestion() {
        return Arrays.asList(
                new GroupFilePairReleaseOcurrenceByQuantity[]{
                    new GroupFilePairReleaseOcurrenceByQuantity(2, 2),
                    new GroupFilePairReleaseOcurrenceByQuantity(3, 3),
                    new GroupFilePairReleaseOcurrenceByQuantity(4, 4),
                    new GroupFilePairReleaseOcurrenceByQuantity(5)
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
        final GroupFilePairReleaseOcurrenceByQuantity other = (GroupFilePairReleaseOcurrenceByQuantity) obj;
        if (!Objects.equals(this.minQuantity, other.minQuantity)) {
            return false;
        }
        if (!Objects.equals(this.maxQuantity, other.maxQuantity)) {
            return false;
        }
        return true;
    }

}
