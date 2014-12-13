package br.edu.utfpr.cm.minerador.services.matrix.model;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairAprioriTest {

    private FilePairApriori instance;

    @Before
    public void setUp() {
        instance = new FilePairApriori(2, 4, 2, 4);
    }

    @After
    public void tearDown() {
        instance = null;
    }

    @Test
    public void testSupportFile1() {
        assertEquals(0.5, instance.getSupportFile(), 0.001);
    }

    @Test
    public void testSupportFile2() {
        assertEquals(1, instance.getSupportFile2(), 0.001);
    }

    @Test
    public void testSupportFilePair() {
        assertEquals(0.5, instance.getSupportFilePair(), 0.001);
    }

    @Test
    public void testConfidenceFile1() {
        assertEquals(1, instance.getConfidence(), 0.001);
    }

    @Test
    public void testConfidenceFile2() {
        assertEquals(0.5, instance.getConfidence2(), 0.001);
    }

    @Test
    public void testLift() {
        assertEquals(1, instance.getLift(), 0.001);
    }

    @Test
    public void testConviction() {
        assertEquals(0, instance.getConviction(), 0.001);
    }

    @Test
    public void testConviction2() {
        assertEquals(0, instance.getConviction2(), 0.0001);
    }

    @Test
    public void testToString() {
        assertEquals("2;4;2;4;0.5;1.0;0.5;1.0;0.5;1.0;0.0;0.0;", instance.toString());
    }
}
