package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairGroup {

    public Collection<FilePairOcurrencesGroup> calculeFilePairReleaseOcurrenceQuantity(List<FilePairReleasesOccurenceCounter> counters, List<GroupFilePairReleaseOcurrenceByQuantity> groupings) {
        Map<GroupFilePairReleaseOcurrenceByQuantity, FilePairOcurrencesGroup> group = new HashMap<>();
        for (FilePairReleasesOccurenceCounter counter : counters) {
            for (GroupFilePairReleaseOcurrenceByQuantity grouping : groupings) {
                if (group.containsKey(grouping)) {
                    group.get(grouping).increment();
                } else {
                    FilePairOcurrencesGroup groupQuantity = new FilePairOcurrencesGroup(grouping);
                    groupQuantity.increment();
                    group.put(grouping, groupQuantity);
                }
            }
        }
        return group.values();
    }
}
