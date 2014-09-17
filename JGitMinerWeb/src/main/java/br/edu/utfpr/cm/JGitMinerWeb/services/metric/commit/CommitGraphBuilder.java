
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
class CommitGraphBuilder {

    public static final String PATH_SEPARATOR = "/";

    static UndirectedGraph<String, String> build(final Set<String> files) {

        UndirectedGraph<String, String> graph = new UndirectedSparseGraph<>();
        // DelegateTree<String, String> graph = new DelegateTree<>();

        for (String file : files) {
            String[] pathToFile = file.split(PATH_SEPARATOR);

            StringBuilder parent = new StringBuilder();

            for (String child : pathToFile) {
                String childPath;
                if (parent.length() > 0) {
                    childPath = parent.toString() + PATH_SEPARATOR + child;
                } else {
                    parent.append(child);
                    continue;
                }

                if (!graph.containsVertex(childPath)) {
                    graph.addEdge(parent.toString() + childPath,
                            parent.toString(), childPath);
                }
                parent.append(PATH_SEPARATOR).append(child);
            }
        }

        // JungExport.exportToImage(graph, new TreeLayout<>(graph), "C:/Users/a562273/Desktop/", "test.png");
        // JungExport.exportToImage(graph, "C:/Users/a562273/Desktop/", "test2.png");
        return graph;
    }

    static UndirectedGraph<String, String> buildAddingRoot(final Set<String> files) {

        UndirectedGraph<String, String> graph = new UndirectedSparseGraph<>();
        // DelegateTree<String, String> graph = new DelegateTree<>();

        String root = "root";
        graph.addVertex(root); // first vertex

        for (String file : files) {
            String[] pathToFile = file.split(PATH_SEPARATOR);

            StringBuilder parent = new StringBuilder(root);

            for (String child : pathToFile) {
                String childPath = parent.toString() + PATH_SEPARATOR + child;
                if (!graph.containsVertex(childPath)) {
                    graph.addEdge(parent.toString() + childPath,
                            parent.toString(), childPath);
                }
                parent.append(PATH_SEPARATOR).append(child);
            }
        }

        // JungExport.exportToImage(graph, new TreeLayout<>(graph), "C:/Users/a562273/Desktop/", "test.png");
        // JungExport.exportToImage(graph, "C:/Users/a562273/Desktop/", "test2.png");
        return graph;
    }
}
