package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the betweenness centrality measure for a graph <code>G</code>.
 *
 * @see edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality
 * @author Rodrigo T. Kuroda
 */
public class BetweennessCalculator {

    /**
     * Calculates betweenness centrality measure for each vertex <code>V</code> of a 
     * graph <code>G</code>.
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>BetweennessMeasure</code>.
     */
    public static <V, E> Map<V, BetweennessMeasure<V>> calcule(final Graph<V, E> graph) {
        
        BetweennessCentrality<V, E> bc = new BetweennessCentrality<>(graph);
        
        Map<V, BetweennessMeasure<V>> result = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
            BetweennessMeasure<V> betweennessMeasure = 
                    new BetweennessMeasure<>(v, bc.getVertexScore(v));
            result.put(v, betweennessMeasure);
        }
        
        return result;
    }
}
