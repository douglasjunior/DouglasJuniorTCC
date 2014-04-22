package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates metrics for ego network (i.e. graph <code>G</code> of a node
 * and its neighborhood).
 *
 * @author Rodrigo T. Kuroda
 */
public class EgoMeasureCalculator {

    /**
     * Calculates metrics for ego network (i.e. graph <code>G</code> of a 
     * node and its neighborhood).
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>EigenvectorMeasure</code>.
     */
    public static <V, E> Map<V, EgoMeasure<V>> calcule(final Graph<V, E> graph) {
        
        EigenvectorCentrality<V, E> ec = new EigenvectorCentrality<>(graph);
        
        Map<V, EgoMeasure<V>> result = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
           
        }
        
        return null;
    }
}
