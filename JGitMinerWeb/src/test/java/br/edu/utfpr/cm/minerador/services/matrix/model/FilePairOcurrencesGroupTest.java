package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOcurrencesGroupTest {

    @Test
    public void testGroupFilePairs() {
        final List<FilterFilePairByReleaseOcurrence> filterSuggestion = new ArrayList<>(FilterFilePairByReleaseOcurrence.getSuggestion());
        filterSuggestion.add(new FilterFilePairByReleaseOcurrence(2, 3));
        FilePairOcurrencesGroup group = new FilePairOcurrencesGroup(filterSuggestion);

        final List<FilePairReleasesOccurenceCounter> filePairOcurrencesCounter = new ArrayList<>();
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addReleaseOcurrence("1.0");
        ab.addReleaseOcurrence("1.1");
        ab.addReleaseOcurrence("1.2");
        filePairOcurrencesCounter.add(ab);

        final FilePairReleasesOccurenceCounter bc = new FilePairReleasesOccurenceCounter(new FilePair("B", "c"));
        bc.addReleaseOcurrence("1.0");
        bc.addReleaseOcurrence("1.1");
        bc.addReleaseOcurrence("1.2");
        filePairOcurrencesCounter.add(bc);

        final FilePairReleasesOccurenceCounter cd = new FilePairReleasesOccurenceCounter(new FilePair("C", "D"));
        cd.addReleaseOcurrence("1.1");
        cd.addReleaseOcurrence("1.2");
        filePairOcurrencesCounter.add(cd);

        final FilePairReleasesOccurenceCounter de = new FilePairReleasesOccurenceCounter(new FilePair("D", "E"));
        de.addReleaseOcurrence("1.1");
        de.addReleaseOcurrence("1.2");
        de.addReleaseOcurrence("1.3");
        de.addReleaseOcurrence("1.4");
        filePairOcurrencesCounter.add(de);

        group.groupFilePairs(filePairOcurrencesCounter);
        Assert.assertEquals(2, group.getQuantity(new FilterFilePairByReleaseOcurrence(3, 3)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 2)));
        Assert.assertEquals(3, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 3)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(4, 4)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(5)));

    }

}
