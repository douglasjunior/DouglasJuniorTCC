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

    @Test
    public void testFitsTrue() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(3, 3);
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addReleaseOcurrence("1.0");
        ab.addReleaseOcurrence("1.1");
        ab.addReleaseOcurrence("1.2");

        Assert.assertTrue(filter.fits(ab));
    }

    @Test
    public void testFitsFalse() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(2, 2);
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addReleaseOcurrence("1.0");
        ab.addReleaseOcurrence("1.1");
        ab.addReleaseOcurrence("1.2");

        Assert.assertFalse(filter.fits(ab));
    }

    @Test
    public void testFitsRangeTrue() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(2, 3);
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addReleaseOcurrence("1.0");
        ab.addReleaseOcurrence("1.1");
        ab.addReleaseOcurrence("1.2");

        Assert.assertTrue(filter.fits(ab));
    }

    @Test
    public void testFitsRangeFalse() {
        final FilterFilePairByReleaseOcurrence filter = new FilterFilePairByReleaseOcurrence(1, 2);
        final FilePairReleasesOccurenceCounter ab = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        ab.addReleaseOcurrence("1.0");
        ab.addReleaseOcurrence("1.1");
        ab.addReleaseOcurrence("1.2");

        Assert.assertFalse(filter.fits(ab));
    }
}
