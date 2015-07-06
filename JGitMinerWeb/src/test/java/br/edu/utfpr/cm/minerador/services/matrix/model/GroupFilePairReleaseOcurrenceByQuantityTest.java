package br.edu.utfpr.cm.minerador.services.matrix.model;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class GroupFilePairReleaseOcurrenceByQuantityTest {

    @Test
    public void testFitsEqualOcurrence() {
        final FilterFilePairByReleaseOcurrence group = new FilterFilePairByReleaseOcurrence(2, 2);
        FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));

        counter.addReleaseOcurrence("1.0");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.1");
        Assert.assertTrue(group.fits(counter));

        counter.addReleaseOcurrence("1.2");
        Assert.assertFalse(group.fits(counter));
    }

    @Test
    public void testFitsMinMaxOcurrence() {
        final FilterFilePairByReleaseOcurrence group = new FilterFilePairByReleaseOcurrence(2, 3);
        FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));
        counter.addReleaseOcurrence("1.0");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.1");
        Assert.assertTrue(group.fits(counter));

        counter.addReleaseOcurrence("1.2");
        Assert.assertTrue(group.fits(counter));

        counter.addReleaseOcurrence("1.3");
        Assert.assertFalse(group.fits(counter));
    }

    @Test
    public void testFitsMinOcurrence() {
        final FilterFilePairByReleaseOcurrence group = new FilterFilePairByReleaseOcurrence(3);

        FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));

        counter.addReleaseOcurrence("1.0");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.1");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.2");
        Assert.assertTrue(new FilterFilePairByReleaseOcurrence(3).fits(counter));

        counter.addReleaseOcurrence("1.3");
        Assert.assertTrue(new FilterFilePairByReleaseOcurrence(3).fits(counter));
    }

    @Test
    public void testHashCode() {
        Assert.assertTrue(new FilterFilePairByReleaseOcurrence(2, 2).hashCode() == new FilterFilePairByReleaseOcurrence(2, 2).hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(new FilterFilePairByReleaseOcurrence(2, 2).equals(new FilterFilePairByReleaseOcurrence(2, 2)));
    }

}
