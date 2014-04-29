package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EigenvectorMeasure class
 * 
 * @author Rodrigo T. Kuroda
 */
public class EigenvectorMeasureTest {

    private EigenvectorMeasure<String> instance;

    @Before
    public void setup() {
        instance = new EigenvectorMeasure("V", 0.1d);
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
        String expResult = "V, eigenvector: 0.1";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method. It is equals if the vertex of two compared 
     * EigenvectorMeasure are equal.
     */
    @Test
    public void testEqualsTrue() {
        EigenvectorMeasure equal = new EigenvectorMeasure<>("V", 0.0d);
        assertEquals(instance, equal);
        assertEquals(instance.hashCode(), equal.hashCode());
    }
    
    /**
     * Test of equals method. It is not equals if the vertex of two compared 
     * EigenvectorMeasure are not equal.
     */
    @Test
    public void testEqualsFalse() {
        EigenvectorMeasure notEqual = new EigenvectorMeasure<>("V2", 0.1d);
        assertNotEquals(instance, notEqual);
        assertNotEquals(instance.hashCode(), notEqual.hashCode());
    }
}
