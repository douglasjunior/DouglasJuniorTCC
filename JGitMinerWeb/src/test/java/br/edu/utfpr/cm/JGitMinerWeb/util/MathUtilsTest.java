package br.edu.utfpr.cm.JGitMinerWeb.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class MathUtilsTest {

    @Test
    /**
     * Test based on http://www.oocities.org/paris/rue/5045/2A9.HTM
     */
    public void testCalculateWeightedGeometricAverage() {
        long[][] values = new long[][]{{1, 2}, {3, 4}, {9, 2}, {27, 1}};
        double result = MathUtils.calculateWeightedGeometricAverage(values);
        Assert.assertEquals(3.8296, result, 4);
    }

}
