
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
    
}
