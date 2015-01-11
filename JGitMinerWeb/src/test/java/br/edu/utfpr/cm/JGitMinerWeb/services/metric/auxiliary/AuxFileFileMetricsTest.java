package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AuxFileFileMetricsTest class.
 * 
 * @author Rodrigo Takashi Kuroda
 */
public class AuxFileFileMetricsTest {
    private AuxFileFileMetrics instance;
    
    @Before
    public void setup() {
        instance = new AuxFileFileMetrics("FileA.java", "FileB.java", 1.0, 2.0, 3.0);
    }
    
    @After
    public void tearDown() {
        instance = null;
    }
    
    /**
     * Test of equals method. It is equals if the (filename 1 and filename 2)
     * equal a (filename 1 and filename 2) or (filename2 and filename1)
     */
    @Test
    public void testEqualsTrue() {
        AuxFileFileMetrics equal1 = new AuxFileFileMetrics("FileA.java", "FileB.java", 3.0, 2.0, 1.0);
        AuxFileFileMetrics equal2 = new AuxFileFileMetrics("FileB.java", "FileA.java", 1.0, 2.0, 3.0);
        
        assertTrue(instance.equals(equal1));
        assertTrue(instance.hashCode() == equal1.hashCode());
        assertTrue(instance.equals(equal2));
        assertTrue(instance.hashCode() == equal2.hashCode());
    }
    
    /**
     * Test of equals method. It is not equals if the (filename 1 and filename 2)
     * not equal a (filename 1 and filename 2) and (filename2 and filename1)
     */
    @Test
    public void testEqualsFalse() {
        AuxFileFileMetrics notEqual = new AuxFileFileMetrics("FileB.java", "FileC.java", 1.0, 2.0, 3.0);
        assertFalse(instance.equals(notEqual));
        assertFalse(instance.hashCode() == notEqual.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("FileA.java;FileB.java;1,0;2,0;3,0;0", instance.toString());
    }
}
