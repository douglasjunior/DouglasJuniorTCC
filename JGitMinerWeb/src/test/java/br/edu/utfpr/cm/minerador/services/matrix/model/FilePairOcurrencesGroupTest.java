package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOcurrencesGroupTest {

    public List<Version> allVersions;

    @Before
    public void setUp() {
        allVersions = new ArrayList<>();
        for (String version : new String[]{"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6"}) {
            allVersions.add(new Version(version));
        }
    }

    @Test
    public void testGroupFilePairs() {
        final List<FilterFilePairByReleaseOcurrence> filterSuggestion = new ArrayList<>(FilterFilePairByReleaseOcurrence.getSuggestedFilters());
        filterSuggestion.add(new FilterFilePairByReleaseOcurrence(2, 3));
        FilePairOcurrencesGroup group = new FilePairOcurrencesGroup(filterSuggestion);

        final List<FilePairReleasesOccurenceCounter> filePairOcurrencesCounter = new ArrayList<>();
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.1"));
        ab.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(ab);

        final FilePairReleasesOccurenceCounter bc = new FilePairReleasesOccurenceCounter(new FilePair("B", "c"));
        bc.addVersionOccurrence(new Version("1.0"));
        bc.addVersionOccurrence(new Version("1.1"));
        bc.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(bc);

        final FilePairReleasesOccurenceCounter cd = new FilePairReleasesOccurenceCounter(new FilePair("C", "D"));
        cd.addVersionOccurrence(new Version("1.1"));
        cd.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(cd);

        final FilePairReleasesOccurenceCounter de = new FilePairReleasesOccurenceCounter(new FilePair("D", "E"));
        de.addVersionOccurrence(new Version("1.1"));
        de.addVersionOccurrence(new Version("1.2"));
        de.addVersionOccurrence(new Version("1.3"));
        de.addVersionOccurrence(new Version("1.4"));
        filePairOcurrencesCounter.add(de);

        group.groupFilePairs(filePairOcurrencesCounter, 1);
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 2)));
        Assert.assertEquals(2, group.getQuantity(new FilterFilePairByReleaseOcurrence(3, 3)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(4, 4)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(5)));
        Assert.assertEquals(3, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 3)));
        Assert.assertEquals("1;2;1;0;3", group.toString());
        Assert.assertEquals("in 2 releases;in 3 releases;in 4 releases;>= 5 releases;in 2-3 releases", group.getDynamicHeader());

    }

    @Test
    public void testGroupFilePairsMoreThanTwoOccurrences() {
        final List<FilterFilePairByReleaseOcurrence> filterSuggestion = new ArrayList<>(FilterFilePairByReleaseOcurrence.getSuggestedFilters());
        filterSuggestion.add(new FilterFilePairByReleaseOcurrence(2, 3));
        FilePairOcurrencesGroup group = new FilePairOcurrencesGroup(filterSuggestion);

        final List<FilePairReleasesOccurenceCounter> filePairOcurrencesCounter = new ArrayList<>();
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.1"));
        ab.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(ab);

        final FilePairReleasesOccurenceCounter bc = new FilePairReleasesOccurenceCounter(new FilePair("B", "C"));
        bc.addVersionOccurrence(new Version("1.0"));
        bc.addVersionOccurrence(new Version("1.1"));
        bc.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(bc);

        final FilePairReleasesOccurenceCounter cd = new FilePairReleasesOccurenceCounter(new FilePair("C", "D"));
        cd.addVersionOccurrence(new Version("1.1"));
        cd.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(cd);

        final FilePairReleasesOccurenceCounter de = new FilePairReleasesOccurenceCounter(new FilePair("D", "E"));
        de.addVersionOccurrence(new Version("1.1"));
        de.addVersionOccurrence(new Version("1.2"));
        de.addVersionOccurrence(new Version("1.3"));
        de.addVersionOccurrence(new Version("1.4"));
        filePairOcurrencesCounter.add(de);

        group.groupFilePairs(filePairOcurrencesCounter, 2);
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 2)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(3, 3)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(4, 4)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(5)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 3)));
        Assert.assertEquals("0;1;0;0;1", group.toString());
        Assert.assertEquals("in 2 releases;in 3 releases;in 4 releases;>= 5 releases;in 2-3 releases", group.getDynamicHeader());

    }

    @Test
    public void testGroupFilePairsMoreThanTwoOccurrencesInVersion() {
        final List<FilterFilePairByReleaseOcurrence> filterSuggestion = new ArrayList<>(FilterFilePairByReleaseOcurrence.getSuggestedFilters());
        filterSuggestion.add(new FilterFilePairByReleaseOcurrence(2, 3));
        FilePairOcurrencesGroup group = new FilePairOcurrencesGroup(filterSuggestion);

        final List<FilePairReleasesOccurenceCounter> filePairOcurrencesCounter = new ArrayList<>();
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"), allVersions);
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.1"));
        ab.addVersionOccurrence(new Version("1.1"));
        ab.addVersionOccurrence(new Version("1.2"));
        ab.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(ab);

        final FilePairReleasesOccurenceCounter bc = new FilePairReleasesOccurenceCounter(new FilePair("B", "C"), allVersions);
        bc.addVersionOccurrence(new Version("1.0"));
        bc.addVersionOccurrence(new Version("1.1"));
        bc.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(bc);

        final FilePairReleasesOccurenceCounter cd = new FilePairReleasesOccurenceCounter(new FilePair("C", "D"), allVersions);
        cd.addVersionOccurrence(new Version("1.1"));
        cd.addVersionOccurrence(new Version("1.2"));
        filePairOcurrencesCounter.add(cd);

        final FilePairReleasesOccurenceCounter de = new FilePairReleasesOccurenceCounter(new FilePair("D", "E"), allVersions);
        de.addVersionOccurrence(new Version("1.1"));
        de.addVersionOccurrence(new Version("1.2"));
        de.addVersionOccurrence(new Version("1.3"));
        de.addVersionOccurrence(new Version("1.4"));
        filePairOcurrencesCounter.add(de);

        group.groupFilePairs(allVersions, filePairOcurrencesCounter, 2);
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 2)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(3, 3)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(4, 4)));
        Assert.assertEquals(0, group.getQuantity(new FilterFilePairByReleaseOcurrence(5)));
        Assert.assertEquals(1, group.getQuantity(new FilterFilePairByReleaseOcurrence(2, 3)));
        Assert.assertEquals("0;1;0;0;1", group.toString());
        Assert.assertEquals("in 2 releases;in 3 releases;in 4 releases;>= 5 releases;in 2-3 releases", group.getDynamicHeader());

    }

    // TODO Testes para o fitsVersionSequenceOccurrences
}
