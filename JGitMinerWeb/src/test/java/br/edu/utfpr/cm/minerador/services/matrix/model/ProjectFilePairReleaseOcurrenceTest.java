package br.edu.utfpr.cm.minerador.services.matrix.model;

import br.edu.utfpr.cm.minerador.services.util.VersionTestHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectFilePairReleaseOcurrenceTest {

    private ProjectFilePairReleaseOcurrence occurrences;

    @Test
    public void testAddVersionForFilePair() {
        occurrences = new ProjectFilePairReleaseOcurrence(new Project("Test"),
                VersionTestHelper.getVersionsAsList("1.0", "1.1", "1.2", "1.3"),
                1, 2,
                FilterFilePairByReleaseOcurrence.getSuggestedFilters(),
                FilterByApriori.getFiltersForExperiment1());

        final FilePair filePairAB = new FilePair("A", "B");
        final Version version10 = new Version("1.0");

        occurrences.addVersionForFilePair(filePairAB, version10);
        Assert.assertEquals(1, occurrences.getOccurrences(filePairAB));
        Assert.assertFalse(occurrences.hasMinimumOccurrencesInOneVersion(filePairAB));

        occurrences.addVersionForFilePair(filePairAB, version10);
        Assert.assertEquals(2, occurrences.getOccurrences(filePairAB));
        Assert.assertTrue(occurrences.hasMinimumOccurrencesInOneVersion(filePairAB));

        Assert.assertTrue(occurrences.hasMinimumOccurrences(filePairAB));
    }
}
