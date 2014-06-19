
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class AuxUserUserDirectionalTest {

    private AuxUserUserDirectional instance;
    private AuxUserUserDirectional instance2;

    @Before
    public void setup() {
        instance = new AuxUserUserDirectional("User1", "Email1", "User2", "Email2");
        instance2 = new AuxUserUserDirectional(null, "Email1", null, "Email2");
    }

    @After
    public void tearDown() {
        instance = null;
        instance2 = null;
    }

    /**
     * Test of equals method. It is equals if the (filename 1 and filename 2)
     * equal a (filename 1 and filename 2) or (filename2 and filename1)
     */
    @Test
    public void testEqualsTrue() {
        AuxUserUserDirectional equal1 = new AuxUserUserDirectional("User1", "User2");
        AuxUserUserDirectional equal2 = new AuxUserUserDirectional("User1", "Email1", "User2", "Email2");
        AuxUserUserDirectional equal3 = new AuxUserUserDirectional(null, "Email1", null, "Email2");

        assertTrue(instance.equals(equal1));
        assertTrue(instance.hashCode() == equal1.hashCode());
        assertTrue(instance.equals(equal2));
        assertTrue(instance.hashCode() == equal2.hashCode());
        assertTrue(instance2.equals(equal3));
        assertTrue(instance2.hashCode() == equal3.hashCode());
    }

    /**
     * Test of equals method. It is not equals if the (filename 1 and filename
     * 2) not equal a (filename 1 and filename 2) and (filename2 and filename1)
     */
    @Test
    public void testEqualsFalse() {
        AuxUserUserDirectional notEqual1 = new AuxUserUserDirectional("User2", "User1");
        AuxUserUserDirectional notEqual2 = new AuxUserUserDirectional(null, "Email1", null, "Email2");
        AuxUserUserDirectional notEqual3 = new AuxUserUserDirectional(null, "Email2", null, "Email1");

        assertFalse(instance.equals(notEqual1));
        assertFalse(instance.hashCode() == notEqual1.hashCode());
        assertFalse(instance.equals(notEqual2));
        assertFalse(instance.hashCode() == notEqual2.hashCode());
        assertFalse(instance2.equals(notEqual3));
        assertFalse(instance2.hashCode() == notEqual3.hashCode());
    }
}
