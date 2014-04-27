
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.global;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for GlobalMeasure class.
 * 
 * @author Rodrigo T. Kuroda
 */
public class GlobalMeasureTest {
    
    private GlobalMeasure instance;
    
    @Before
    public void setup() {
        instance = new GlobalMeasure(0, 1, 2.0d);
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
        String expResult = "size: 0, ties: 1, diameter: 2.0";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of equals method. It is not equals if the vertex of two compared 
     * GlobalMeasure are not equal.
     */
    @Test
    public void testEqualsFalse() {
        GlobalMeasure notEqual = new GlobalMeasure(0, 1, 2.0d);
        
        assertNotEquals(instance, notEqual);
    }
    
    /**
     * Test of pairs calculation: size * (size - 1)
     */
    @Test
    public void testPairs() {
        GlobalMeasure test1 = new GlobalMeasure(2, 2, 0.0d);
        GlobalMeasure test2 = new GlobalMeasure(10, 5, 0.0d);
        GlobalMeasure test3 = new GlobalMeasure(1, 1, 0.0d);
        assertEquals(2, test1.getPairs());
        assertEquals(90, test2.getPairs());
        assertEquals(0, test3.getPairs());
    }
    
    /**
     * Test of density calculation: ties / pairs
     */
    @Test
    public void testDensity() {
        GlobalMeasure test1 = new GlobalMeasure(2, 2, 0.0d);
        GlobalMeasure test2 = new GlobalMeasure(10, 5, 0.0d);
        GlobalMeasure test3 = new GlobalMeasure(1, 1, 0.0d);
        assertEquals(1.0d, test1.getDensity(), 0.001);
        assertEquals(0.055d, test2.getDensity(), 0.001);
        assertEquals(1, test3.getDensity(), 0.001);
    }
}
