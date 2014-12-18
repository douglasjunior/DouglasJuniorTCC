package br.edu.utfpr.cm.minerador.services.matrix.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairTest {

    private FilePair instance;

    @Before
    public void setUp() {
        instance = new FilePair("A", "B");
    }

    @After
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testEquals() {
        FilePair ab = new FilePair("A", "B");
        FilePair ba = new FilePair("B", "A");
        assertTrue(instance.equals(ab));
        assertTrue(instance.equals(ba));

        assertEquals(instance.hashCode(), ab.hashCode());
        assertEquals(instance.hashCode(), ba.hashCode());
    }

    @Test
    public void testNotEquals() {
        FilePair ac = new FilePair("A", "C");
        FilePair bc = new FilePair("B", "C");
        assertFalse(instance.equals(ac));
        assertFalse(instance.equals(bc));

        assertNotEquals(instance.hashCode(), ac.hashCode());
        assertNotEquals(instance.hashCode(), bc.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("A;B;", instance.toString());
    }

    @Test
    public void testToStringAprioriBasedBA() {
        FilePairApriori filePairApriori = new FilePairApriori(2, 1, 1, 3);
        assertEquals("B;A;", instance.toString(filePairApriori));
    }

    @Test
    public void testToStringAprioriBasedAB() {
        FilePairApriori filePairApriori = new FilePairApriori(1, 2, 1, 3);
        assertEquals("A;B;", instance.toString(filePairApriori));
    }

    @Test
    public void testConstructorAprioriBasedBA() {
        FilePairApriori apriori = new FilePairApriori(2, 1, 1, 3);
        FilePair filePairAB = new FilePair("A", "B", apriori);
        assertTrue(apriori.getConfidence() < apriori.getConfidence2());
        assertEquals("B;A;", filePairAB.toString());
    }

    @Test
    public void testConstructorAprioriBasedAB() {
        FilePairApriori apriori = new FilePairApriori(1, 2, 1, 3);
        FilePair filePairBA = new FilePair("A", "B", apriori);
        assertTrue(apriori.getConfidence() >= apriori.getConfidence2());
        assertEquals("A;B;", filePairBA.toString());
    }

    @Test
    public void testOrderAprioriBasedBA() {
        FilePairApriori apriori = new FilePairApriori(2, 1, 1, 3);
        FilePair filePairAB = new FilePair("A", "B");
        filePairAB.orderFilePairByConfidence(apriori);
        assertTrue(apriori.getConfidence() < apriori.getConfidence2());
        assertEquals("B;A;", filePairAB.toString());
    }

    @Test
    public void testOrderAprioriBasedAB() {
        FilePairApriori apriori = new FilePairApriori(1, 2, 1, 3);
        FilePair filePairBA = new FilePair("A", "B", apriori);
        filePairBA.orderFilePairByConfidence(apriori);
        assertTrue(apriori.getConfidence() >= apriori.getConfidence2());
        assertEquals("A;B;", filePairBA.toString());
    }
}
