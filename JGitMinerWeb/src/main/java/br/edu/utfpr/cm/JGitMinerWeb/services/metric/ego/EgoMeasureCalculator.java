package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
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
     * The following metrics are calculate:
     * - size
     * - ties
     * - ego betweenness centrality
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>EigenvectorMeasure</code>.
     */
    public static <V, E> Map<V, EgoMeasure<V>> calcule(final Graph<V, E> graph) {
        
        final Map<V, Graph<V, E>> egoNetworks = 
                EgoNetworkExtractor.extractEgoNetwork(graph);
        
        final Map<V, EgoMeasure<V>> result = 
                new HashMap<>(graph.getVertexCount());
        
        for (V v : graph.getVertices()) {
            final Graph<V, E> egoNetwork = egoNetworks.get(v);
            
            final BetweennessCentrality<V, E> bc = 
                    new BetweennessCentrality<>(egoNetwork);
            
            result.put(v, new EgoMeasure<>(v, egoNetwork.getVertexCount(), 
                    egoNetwork.getEdgeCount(), bc.getVertexScore(v)));
        }
        
        return result;
    }
}
