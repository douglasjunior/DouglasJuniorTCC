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
        final GroupFilePairReleaseOcurrenceByQuantity group = new GroupFilePairReleaseOcurrenceByQuantity(2, 2);
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
        final GroupFilePairReleaseOcurrenceByQuantity group = new GroupFilePairReleaseOcurrenceByQuantity(2, 3);
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
        final GroupFilePairReleaseOcurrenceByQuantity group = new GroupFilePairReleaseOcurrenceByQuantity(3);

        FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(new FilePair("A", "B"));

        counter.addReleaseOcurrence("1.0");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.1");
        Assert.assertFalse(group.fits(counter));

        counter.addReleaseOcurrence("1.2");
        Assert.assertTrue(new GroupFilePairReleaseOcurrenceByQuantity(3).fits(counter));

        counter.addReleaseOcurrence("1.3");
        Assert.assertTrue(new GroupFilePairReleaseOcurrenceByQuantity(3).fits(counter));
    }

    @Test
    public void testHashCode() {
        Assert.assertTrue(new GroupFilePairReleaseOcurrenceByQuantity(2, 2).hashCode() == new GroupFilePairReleaseOcurrenceByQuantity(2, 2).hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(new GroupFilePairReleaseOcurrenceByQuantity(2, 2).equals(new GroupFilePairReleaseOcurrenceByQuantity(2, 2)));
    }

}
