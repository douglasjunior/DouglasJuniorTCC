package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOcurrencesGroup {

    private final Map<FilterFilePairByReleaseOcurrence, AtomicInteger> groupingCount;

    public FilePairOcurrencesGroup(List<FilterFilePairByReleaseOcurrence> groupingList) {
        this.groupingCount = new LinkedHashMap<>();
        for (FilterFilePairByReleaseOcurrence grouping : groupingList) {
            groupingCount.put(grouping, new AtomicInteger());
        }
    }

    public int getQuantity(FilterFilePairByReleaseOcurrence filter) {
        if (groupingCount.get(filter) != null) {
            return groupingCount.get(filter).get();
        }
        throw new IllegalArgumentException("Filter for group does not exists.");
    }

    private void increment(FilterFilePairByReleaseOcurrence filter) {
        groupingCount.get(filter).incrementAndGet();
    }

    public void groupFilePairs(Collection<FilePairReleasesOccurenceCounter> counters) {
        for (FilePairReleasesOccurenceCounter counter : counters) {
            for (FilterFilePairByReleaseOcurrence group : groupingCount.keySet()) {
                if (group.fits(counter)) {
                    increment(group);
                }
            }
        }
    }
}
