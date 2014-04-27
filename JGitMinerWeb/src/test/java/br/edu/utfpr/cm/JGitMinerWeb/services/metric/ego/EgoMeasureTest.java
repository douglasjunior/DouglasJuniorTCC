package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EgoMeasure class
 * 
 * @author Rodrigo T. Kuroda
 */
public class EgoMeasureTest {

    private EgoMeasure<String> instance;

    @Before
    public void setup() {
        instance = new EgoMeasure("V", 1, 1, 2.0d);
    }
    
    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of toString method
     */
    @Test
    public void testToString() {
        String expResult = "V, size: 1, ties: 1, pairs: 0, density: 1.0, ego betweeness centrality: 2.0";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method. It is equals if the vertex of two compared 
     * EgoMeasure are equal.
     */
    @Test
    public void testEqualsTrue() {
        EgoMeasure equal = new EgoMeasure<>("V", 1, 1, 2.0d);
        assertTrue(instance.equals(equal));
        assertTrue(instance.hashCode() == equal.hashCode());
    }
    
    /**
     * Test of equals method. It is not equals if the vertex of two compared 
     * EgoMeasure are not equal.
     */
    @Test
    public void testEqualsFalse() {
        EgoMeasure notEqual = new EgoMeasure<>("V2", 0, 0, 0d);
        assertFalse(instance.equals(notEqual));
        assertFalse(instance.hashCode() == notEqual.hashCode());
    }
}
