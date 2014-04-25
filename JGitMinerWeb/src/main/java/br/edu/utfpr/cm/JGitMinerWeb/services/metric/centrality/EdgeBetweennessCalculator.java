package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the betweenness centrality measure for each edge <code>E</code>.
 *
 * @see edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality
 * @author Rodrigo T. Kuroda
 */
public class EdgeBetweennessCalculator {

    /**
     * Calculates betweenness centrality measure for each edge <code>E</code> 
     * of a graph <code>G</code>.
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the edge (E) and the value is 
     *      a POJO with result of metrics, named <code>EdgeBetweennessMeasure</code>.
     */
    public static <V, E> Map<E, EdgeBetweennessMeasure<E>> calcule(final Graph<V, E> graph) {
        
        BetweennessCentrality<V, E> bc = new BetweennessCentrality<>(graph);
        
        Map<E, EdgeBetweennessMeasure<E>> result = new HashMap<>(graph.getVertexCount());
        for (E e : graph.getEdges()) {
            EdgeBetweennessMeasure<E> edgeBetweennessMeasure = 
                    new EdgeBetweennessMeasure<>(e, bc.getEdgeScore(e));
            result.put(e, edgeBetweennessMeasure);
        }
        
        return result;
    }
}
