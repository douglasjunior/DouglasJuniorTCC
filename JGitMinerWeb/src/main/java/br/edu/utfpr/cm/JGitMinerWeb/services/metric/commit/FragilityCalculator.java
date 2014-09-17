
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedGraph;
import java.util.List;
import java.util.Set;

/**
 * Martin define a fragilidade como "a tendência de um programa quebrar em
 * muitas diferentes partes quando uma mudança é realizada. Frequentemente,
 * novos problemas ocorrem em partes que não tem relacionamento conceitual com
 * as partes modificadas..." (Martin and Martin 2006). O projeto do software
 * apresenta sintomas de fragilidade quando o software começa a falhar em muitas
 * partes diferentes para a maioria das mudanças. A fragilidade é calculada pela
 * distância entre o caminho dos * artefatos de um commit percorrendo a árvore
 * de diretórios dos artefatos.
 *
 * Build a graph for a file list that had been committed. Based on graph, the
 * path between the coupled file
 *
 * @author Rodrigo T. Kuroda
 */
public class FragilityCalculator {

    private final DijkstraShortestPath<String, String> shortestPath;

    public FragilityCalculator(Set<String> files, boolean addRoot) {
        UndirectedGraph<String, String> graph;
        if (addRoot) {
            graph = CommitGraphBuilder.buildAddingRoot(files);
        } else {
            graph = CommitGraphBuilder.build(files);
        }
        this.shortestPath = new DijkstraShortestPath<>(graph);
    }

    public FragilityCalculator(UndirectedGraph<String, String> graph) {
        this.shortestPath = new DijkstraShortestPath<>(graph);
    }

    public double calcule(final Set<AuxFileFile> files) {
        int pathSum = 0;
        for (AuxFileFile auxFileFile : files) {
            List<String> edges = shortestPath.getPath(auxFileFile.getFileName(), auxFileFile.getFileName2());
            pathSum += edges.size();
        }
        return (double) pathSum / files.size();
    }
}
