package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AuxFileFileIssueMetricsTest class.
  * 
 * @author Rodrigo Takashi Kuroda
 */
public class AuxFileFileIssueMetricsTest {

    private AuxFileFileIssueMetrics instance;
    
    @Before
    public void setup() {
        instance = new AuxFileFileIssueMetrics("FileA.java", "FileB.java", 1, 1.0, 2.0, 3.0);
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
        AuxFileFileIssueMetrics equal1 = new AuxFileFileIssueMetrics("FileA.java", "FileB.java", 1, 3.0, 2.0, 1.0);
        AuxFileFileIssueMetrics equal2 = new AuxFileFileIssueMetrics("FileB.java", "FileA.java", 1, 1.0, 2.0, 3.0);
        
        assertEquals(true, instance.equals(equal1));
        assertEquals(true, instance.hashCode() == equal1.hashCode());
        assertEquals(true, instance.equals(equal2));
        assertEquals(true, instance.hashCode() == equal2.hashCode());
    }
    
    /**
     * Test of equals method. It is not equals if the (filename 1 and filename 2)
     * not equal a (filename 1 and filename 2) and (filename2 and filename1)
     */
    @Test
    public void testEqualsFalse() {
        AuxFileFileIssueMetrics notEqual = new AuxFileFileIssueMetrics("FileB.java", "FileC.java", 1, 1.0, 2.0, 3.0);
        AuxFileFileIssueMetrics notEqual2 = new AuxFileFileIssueMetrics("FileA.java", "FileB.java", 2, 1.0, 2.0, 3.0);
        AuxFileFileIssueMetrics notEqual3 = new AuxFileFileIssueMetrics("FileB.java", "FileA.java", 2, 1.0, 2.0, 3.0);
        assertEquals(false, instance.equals(notEqual));
        assertEquals(false, instance.hashCode() == notEqual.hashCode());
        assertEquals(false, instance.equals(notEqual2));
        assertEquals(false, instance.hashCode() == notEqual2.hashCode());
        assertEquals(false, instance.equals(notEqual3));
        assertEquals(false, instance.hashCode() == notEqual3.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("FileA.java;FileB.java;;;;;;0;1.0;2.0;3.0;0", instance.toString());
    }
}
