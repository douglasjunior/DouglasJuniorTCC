package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ListUtilsTest {

    @Test
    public void testOrdering() {
        List<Integer> fileFileMetrics
                = Arrays.asList(35, 100, 30, 15, 60, 20, 60, 10, 5, 0, 40, 25, 90, 70, 35, 45);
        List<Integer> ordered25percent
                = ListUtils.getTopMost25Percent(fileFileMetrics, new Comparator<Integer>() {

            @Override
            public int compare(Integer leftValue, Integer rightValue) {
                if (leftValue > rightValue) {
                    return -1;
                } else if (leftValue < rightValue) {
                    return 1;
                }
                return 0;
            }
        });

        assertEquals(Arrays.asList(100, 90, 70, 60), ordered25percent);
    }

}
