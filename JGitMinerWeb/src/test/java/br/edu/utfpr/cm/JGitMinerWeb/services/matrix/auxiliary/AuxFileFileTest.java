package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
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
        AuxFileFile notEqual = new AuxFileFile("FileB.java", "FileC.java");
        assertFalse(instance.equals(notEqual));
        assertFalse(instance.hashCode() == notEqual.hashCode());
    }

    @Test
    public void testAddIssueId() {
        AuxFileFile fileFile = new AuxFileFile("FileA.java", "FileB.java");
        fileFile.addIssueId(1);
        fileFile.addIssueId(2);

        Set<Integer> issuesExpected = new HashSet<>(2);
        issuesExpected.add(1);
        issuesExpected.add(2);

        int weightExpected = 2;

        assertEquals(issuesExpected, fileFile.getIssuesId());
        assertEquals(weightExpected, fileFile.getIssuesWeight());
    }

    @Test
    @Ignore // TODO
    public void testToString() {
        AuxFileFile fileFile = new AuxFileFile("FileA.java", "FileB.java");
        fileFile.addIssueId(1);
        fileFile.addIssueId(2);

        String toStringExpected = "FileA.java;FileB.java;2;1,2;0;;0;;0.0";

        assertEquals(toStringExpected, fileFile.toString());
    }
}
