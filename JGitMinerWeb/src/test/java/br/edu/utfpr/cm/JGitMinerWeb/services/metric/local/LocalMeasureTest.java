
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Basic tests for LocalMeasure class.
 * 
 * @author Rodrigo T. Kuroda
 */
public class LocalMeasureTest {
    
    /**
     * Test of equals method, of class LocalMeasure.
     */
    @Test
    public void testEqualsTrue() {
        LocalMeasure<String> v1 = new LocalMeasure<>("V", 0, 0, 0);
        LocalMeasure<String> v2 = new LocalMeasure<>("V", 0, 0, 0);
        
        assertTrue(v1.equals(v2));
    }
    
    /**
     * Test of equals method, of class LocalMeasure.
     */
    @Test
    public void testEqualsFalse() {
        LocalMeasure<String> v1 = new LocalMeasure<>("V1", 0, 0, 0);
        LocalMeasure<String> v2 = new LocalMeasure<>("V2", 0, 0, 0);
        
        assertFalse(v1.equals(v2));
    }
    
}
