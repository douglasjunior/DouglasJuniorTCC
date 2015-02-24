package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AuxFileFilePullTest class.
  * 
 * @author Rodrigo Takashi Kuroda
 */
public class AuxFileFilePullTest {

    private AuxFileFileIssue instance1;
    
    @Before
    public void setup() {
        instance1 = new AuxFileFileIssue("FileA.java", "FileB.java", 1);
    }
    
    @After
    public void tearDown() {
        instance1 = null;
    }
    
    /**
     * Test of equals method. It is equals if the (filename 1 and filename 2)
     * equal a (filename 1 and filename 2) or (filename2 and filename1)
     */
    @Test
    public void testEqualsTrue() {
        AuxFileFileIssue equal1 = new AuxFileFileIssue("FileA.java", "FileB.java", 1);
        AuxFileFileIssue equal2 = new AuxFileFileIssue("FileB.java", "FileA.java", 1);
        
        assertTrue(instance1.equals(equal1));
        assertTrue(instance1.hashCode() == equal1.hashCode());
        assertTrue(instance1.equals(equal2));
        assertTrue(instance1.hashCode() == equal2.hashCode());
    }
    
    /**
     * Test of equals method. It is not equals if the (filename 1 and filename 2)
     * not equal a (filename 1 and filename 2) and (filename2 and filename1)
     */
    @Test
    public void testEqualsFalse() {
        AuxFileFileIssue notEqual = new AuxFileFileIssue("FileB.java", "FileC.java", 1);
        AuxFileFileIssue notEqual2 = new AuxFileFileIssue("FileA.java", "FileB.java", 2);
        assertFalse(instance1.equals(notEqual));
        assertFalse(instance1.hashCode() == notEqual.hashCode());
        assertFalse(instance1.equals(notEqual2));
        assertFalse(instance1.hashCode() == notEqual2.hashCode());
    }
}
