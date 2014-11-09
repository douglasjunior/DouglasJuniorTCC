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
public class FilePathTest {

    private static FilePath instance;
    private static final String FILE_PATH = "br/edu/utfpr/cm/Test.java";
    private static final Integer COMMIT_ID = 1000;
    private static final Integer FILE_ID = 10;

    @BeforeClass
    public static void setUp() {
        instance = new FilePath(COMMIT_ID, FILE_ID, FILE_PATH);
    }

    @AfterClass
    public static void tearDown() {
        instance = null;
    }

    @Test
    public void testGetCommitId() {
        Integer result = instance.getCommitId();
        assertEquals(COMMIT_ID, result);
    }

    @Test
    public void testGetFileId() {
        Integer result = instance.getFileId();
        assertEquals(FILE_ID, result);
    }

    @Test
    public void testGetFilePath() {
        String result = instance.getFilePath();
        assertEquals(FILE_PATH, result);
    }

    @Test
    public void testEquals() {
        FilePath equal = new FilePath(1000, 10, "Test.java");
        boolean result = instance.equals(equal);
        assertEquals(true, result);
        assertEquals(instance.hashCode(), equal.hashCode());
    }

    @Test
    public void testNotEquals1() {
        FilePath notEqual = new FilePath(1001, 10, FILE_PATH);

        boolean result1 = instance.equals(notEqual);
        assertEquals(false, result1);
        assertNotEquals(instance.hashCode(), notEqual.hashCode());
    }

    @Test
    public void testNotEquals2() {
        FilePath notEqual = new FilePath(1000, 11, FILE_PATH);

        boolean result = instance.equals(notEqual);
        assertEquals(false, result);
        assertNotEquals(instance.hashCode(), notEqual.hashCode());
    }
}
