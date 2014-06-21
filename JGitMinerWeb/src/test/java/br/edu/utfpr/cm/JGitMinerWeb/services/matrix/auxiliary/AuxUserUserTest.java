
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class AuxUserUserTest {
    private AuxUserUser instance;

    @Before
    public void setup() {
        instance = new AuxUserUser("User1", "User2");
    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of equals method. It is equals if the (username 1 and username 2)
     * equal a (username 1 and username 2) or (username2 and username1)
     */
    @Test
    public void testEqualsTrue() {
        AuxUserUser equal1 = new AuxUserUser("User1", "User2");
        AuxUserUser equal2 = new AuxUserUser("User2", "User1");

        assertTrue(instance.equals(equal1));
        assertTrue(instance.hashCode() == equal1.hashCode());
        assertTrue(instance.equals(equal2));
        assertTrue(instance.hashCode() == equal2.hashCode());
    }

    /**
     * Test of equals method. It is not equals if the (username 1 and username
     * 2) not equal a (username 1 and username 2) and (username 2 and username
     * 1)
     */
    @Test
    public void testEqualsFalse() {
        AuxUserUser notEqual = new AuxUserUser("Email1", "Email2");

        assertFalse(instance.equals(notEqual));
        assertFalse(instance.hashCode() == notEqual.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("User1;User2", instance.toStringUserAndUser2());
        assertEquals("User2;User1", instance.toStringUser2AndUser());
        assertEquals("User1;User2", instance.toString());
    }
}
