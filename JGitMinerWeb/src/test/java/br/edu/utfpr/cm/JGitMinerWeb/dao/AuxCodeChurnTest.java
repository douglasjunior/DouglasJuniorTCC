package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.minerador.services.metric.model.CodeChurn;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class AuxCodeChurnTest {
    private CodeChurn instance1;
    private CodeChurn instance2;

    @Before
    public void setup() {
        instance1 = new CodeChurn("FileA.java", 1, 2);
        instance2 = new CodeChurn("FileA.java", "FileB.java", 1, 2);
    }

    @After
    public void tearDown() {
        instance1 = null;
        instance2 = null;
    }

    @Test
    public void testGetAdditionsNormalized() {
        double expResult = 0.3333;
        double result = instance1.getAdditionsNormalized();
        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testGetDeletionsNormalized() {
        double expResult = 0.6666;
        double result = instance1.getDeletionsNormalized();
        assertEquals(expResult, result, 0.0001);
    }

    @Test
    public void testHashCode() {
        CodeChurn instance = new CodeChurn("FileA.java", 10, 20);
        int expResult = instance1.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testEquals() {
        CodeChurn equal1 = new CodeChurn("FileA.java", 10, 20);
        CodeChurn equal2 = new CodeChurn("FileB.java", "FileA.java", 10, 20);
        boolean expResult = true;
        boolean result1 = instance1.equals(equal1);
        boolean result2 = instance2.equals(equal2);
        assertEquals(expResult, result1);
        assertEquals(expResult, result2);
    }

    @Test
    public void testNotEquals() {
        CodeChurn notEqual1 = new CodeChurn("FileA.java", "FileB.java", 10, 20);
        CodeChurn notEqual2 = new CodeChurn("FileA.java", 10, 20);
        boolean expResult = false;
        boolean result1 = instance1.equals(notEqual1);
        boolean result2 = instance2.equals(notEqual2);
        assertEquals(expResult, result1);
        assertEquals(expResult, result2);
    }

}
