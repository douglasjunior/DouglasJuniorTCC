package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the betweenness centrality measure for a graph <code>G</code>.
 *
 * @see edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality
 * @author Rodrigo T. Kuroda
 */
public class DegreeCalculator {

    /**
     * Calculates degree centrality measure for each vertex <code>V</code> of a 
     * graph <code>G</code>.
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>BetweennessMeasure</code>.
     */
    public static <V, E> Map<V, Integer> calcule(final Graph<V, E> graph) {

        if (graph == null) {
            return new HashMap<>(1);
        }

        DegreeScorer<V> ds = new DegreeScorer<>(graph);
        
        Map<V, Integer> result = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
            result.put(v, ds.getVertexScore(v));
        }
        
        return result;
    }
}
