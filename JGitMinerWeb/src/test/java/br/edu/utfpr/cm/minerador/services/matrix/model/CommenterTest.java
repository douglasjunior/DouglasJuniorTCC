package br.edu.utfpr.cm.minerador.services.matrix.model;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommenterTest {

    private static Commenter instance;
    private static final String NAME = "Tester";
    private static final String EMAIL = "tester@test.com";
    private static final Integer COMMENTER_ID = 10;

    @BeforeClass
    public static void setUp() {
        instance = new Commenter(COMMENTER_ID, NAME, EMAIL);
    }

    @AfterClass
    public static void tearDown() {
        instance = null;
    }

    @Test
    public void testGetId() {
        Integer result = instance.getId();
        assertEquals(COMMENTER_ID, result);
    }

    @Test
    public void testGetName() {
        String result = instance.getName();
        assertEquals(NAME, result);
    }

    @Test
    public void testGetEmail() {
        String result = instance.getEmail();
        assertEquals(EMAIL, result);
    }

    @Test
    public void testEquals() {
        Commenter equal = new Commenter(COMMENTER_ID, "Tester Jr", "testerjr@test.com");
        boolean result = instance.equals(equal);
        assertEquals(true, result);
        assertEquals(instance.hashCode(), equal.hashCode());
    }

    @Test
    public void testNotEquals1() {
        Commenter notEqual = new Commenter(1, NAME, EMAIL);

        boolean result1 = instance.equals(notEqual);
        assertEquals(false, result1);
        assertNotEquals(instance.hashCode(), notEqual.hashCode());
    }
}
