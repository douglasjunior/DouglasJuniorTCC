package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FragilityCalculatorTest {

    private static Set<String> files;

    @Test
    public void testCalculeBuildingGraphWithoutAddingRoot() {
        // co-changed files in same commit
        Set<AuxFileFile> pairFiles = new HashSet<>();
        // path = 2 + 1 = 3
        pairFiles.add(new AuxFileFile("A/B/Y", "A/B/D/X"));
        // path = 2 + 4 = 6 (+3 = 9)
        pairFiles.add(new AuxFileFile("A/B/D/X", "A/C/E/F/Z"));
        // path = 3 + 4 = 7 (+9 = 16)
        pairFiles.add(new AuxFileFile("A/C/E/F/Z", "A/B/Y"));
        // fragility = sum(path) / pair of files = 16 / 3 = 5,333.

        FragilityCalculator fc = new FragilityCalculator(files, false);
        double fragility = fc.calcule(pairFiles);
        Assert.assertEquals(5.3333, fragility, 0.0001);
    }

    @Test
    public void testCalculeBuildingGraphAddingRoot() {
        // co-changed files in same commit
        Set<AuxFileFile> pairFiles = new HashSet<>();
        // path = 2 + 1 = 3
        pairFiles.add(new AuxFileFile("root/A/B/Y", "root/A/B/D/X"));
        // path = 2 + 4 = 6 (+3 = 9)
        pairFiles.add(new AuxFileFile("root/A/B/D/X", "root/A/C/E/F/Z"));
        // path = 3 + 4 = 7 (+9 = 16)
        pairFiles.add(new AuxFileFile("root/A/C/E/F/Z", "root/A/B/Y"));
        // fragility = sum(path) / pair of files = 16 / 3 = 5,333.

        FragilityCalculator fc = new FragilityCalculator(files, true);
        double fragility = fc.calcule(pairFiles);
        Assert.assertEquals(5.3333, fragility, 0.0001);
    }

    @BeforeClass
    public static void setUp() {
        // files committed in same commit
        files = new HashSet<>();
        files.add("A/B/Y");
        files.add("A/B/Y2");
        files.add("A/B/D/X");
        files.add("A/B/D/X2");
        files.add("A/C/E/W");
        files.add("A/C/E/F/Z");
        files.add("A/C/E/F/Z0");
        files.add("A/C/E/F/Z2");
    }

}
