package br.edu.utfpr.cm.JGitMinerWeb.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PathUtilsTest {

    @Test
    public void testIsSameFullPath() {
        String file1 = "src/main/java/br/com/utfpr/a/A.java";
        String file2 = "src/main/java/br/com/utfpr/a/B.java";
        String file3 = "src/main/java/br/com/utfpr/b/A.java";
        String file4 = "src/main/java/br/com/utfpr/b/B.java";

        Assert.assertTrue(PathUtils.isSameFullPath(file1, file2));
        Assert.assertTrue(PathUtils.isSameFullPath(file3, file4));
        Assert.assertFalse(PathUtils.isSameFullPath(file1, file3));
        Assert.assertFalse(PathUtils.isSameFullPath(file1, file4));
        Assert.assertFalse(PathUtils.isSameFullPath(file2, file3));
        Assert.assertFalse(PathUtils.isSameFullPath(file2, file4));
    }
}
