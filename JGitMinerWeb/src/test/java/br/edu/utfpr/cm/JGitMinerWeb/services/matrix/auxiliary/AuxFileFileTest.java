package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AuxFileFileTest class.
 * 
 * @author Rodrigo Takashi Kuroda
 */
public class AuxFileFileTest {
    private AuxFileFile instance;
    
    @Before
    public void setup() {
        instance = new AuxFileFile("FileA.java", "FileB.java");
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
        AuxFileFile equal1 = new AuxFileFile("FileA.java", "FileB.java");
        AuxFileFile equal2 = new AuxFileFile("FileB.java", "FileA.java");
        
        assertEquals(instance, equal1);
        assertEquals(instance, equal2);
    }
    
    /**
     * Test of equals method. It is not equals if the (filename 1 and filename 2)
     * not equal a (filename 1 and filename 2) and (filename2 and filename1)
     */
    @Test
    public void testEqualsFalse() {
        AuxFileFile notEqual = new AuxFileFile("FileB.java", "FileC.java");
        
        assertNotEquals(instance, notEqual);
    }
}
