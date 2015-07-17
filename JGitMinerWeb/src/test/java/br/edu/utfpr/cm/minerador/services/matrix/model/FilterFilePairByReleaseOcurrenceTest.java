package br.edu.utfpr.cm.minerador.services.matrix.model;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilterFilePairByReleaseOcurrenceTest {

    public FilterFilePairByReleaseOcurrenceTest() {
    }

    private FilePairReleasesOccurenceCounter getSampleValues() {
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addVersionOccurrence(new Version("1.0"));
        ab.addVersionOccurrence(new Version("1.1"));
        ab.addVersionOccurrence(new Version("1.2"));
        return ab;
    }

    @Test
    public void testFitsTrue() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(3, 3);
        FilePairReleasesOccurenceCounter ab = getSampleValues();

        Assert.assertTrue(filter.fits(ab));
    }

    @Test
    public void testFitsFalse() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(2, 2);
        FilePairReleasesOccurenceCounter ab = getSampleValues();

        Assert.assertFalse(filter.fits(ab));
    }

    @Test
    public void testFitsRangeTrue() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(2, 3);
        FilePairReleasesOccurenceCounter ab = getSampleValues();

        Assert.assertTrue(filter.fits(ab));
    }

    @Test
    public void testFitsRangeFalse() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(1, 2);
        FilePairReleasesOccurenceCounter ab = getSampleValues();

        Assert.assertFalse(filter.fits(ab));
    }
}
