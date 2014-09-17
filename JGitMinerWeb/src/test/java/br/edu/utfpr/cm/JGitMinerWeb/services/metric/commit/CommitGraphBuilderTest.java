package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

import edu.uci.ics.jung.graph.UndirectedGraph;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommitGraphBuilderTest {

    private static Set<String> files;

    @Test
    public void testBuildGraphWithoutAddingRoot() {
        UndirectedGraph<String, String> graph = CommitGraphBuilder.build(files);
        Assert.assertEquals(14, graph.getVertexCount());
    }

    @Test
    public void testBuildGraphAddingRoot() {
        UndirectedGraph<String, String> graph = CommitGraphBuilder.buildAddingRoot(files);
        Assert.assertEquals(15, graph.getVertexCount());
    }

    @BeforeClass
    public static void setUp() {
        files = new HashSet<>();
        files.add("A/B/Y"); // +3 nodes = 3
        files.add("A/B/Y2"); // +1 node = 4
        files.add("A/B/D/X"); // +2 nodes = 6
        files.add("A/B/D/X2"); // +1 node = 7
        files.add("A/C/E/W"); // +3 nodes = 10
        files.add("A/C/E/F/Z"); // +2 nodes = 12
        files.add("A/C/E/F/Z0"); // +1 node = 13
        files.add("A/C/E/F/Z2"); // +1 node = 14
        // +1 root node = 15
    }
}
