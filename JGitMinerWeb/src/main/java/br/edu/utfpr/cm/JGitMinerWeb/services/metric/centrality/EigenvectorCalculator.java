package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the eigenvector measure for a graph <code>G</code>.
 *
 * @see edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality
 * @author Rodrigo T. Kuroda
 */
public class EigenvectorCalculator {

    /**
     * Calculates eigenvector measure for each vertex <code>V</code> of a 
     * graph <code>G</code>.
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>EigenvectorMeasure</code>.
     */
    public static <V, E> Map<V, EigenvectorMeasure<V>> calcule(final Graph<V, E> graph) {
        
        EigenvectorCentrality<V, E> ec = new EigenvectorCentrality<>(graph);
        
        Map<V, EigenvectorMeasure<V>> result = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
            EigenvectorMeasure<V> eigenvectorMeasure = 
                    new EigenvectorMeasure<>(v, ec.getVertexScore(v));
            result.put(v, eigenvectorMeasure);
        }
        
        return result;
    }
}
