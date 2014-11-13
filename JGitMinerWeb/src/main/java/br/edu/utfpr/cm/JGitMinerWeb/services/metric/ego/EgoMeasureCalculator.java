package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 * Measures the properties of each node's ego network 
 * (i.e. graph <code>G</code> of a node and its neighborhood).
 * 
 * The following metrics are calculate:
 * - size
 * - pairs
 * - ties
 * - density
 * - ego betweenness centrality
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

        if (graph == null) {
            return new HashMap<>(1);
        }

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
    
    /**
     * Calculates metrics for ego network (i.e. graph <code>G</code> of a 
     * node and its neighborhood).
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @param edgeWeigth A Map of weight for each edge (E).
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>EigenvectorMeasure</code>.
     */
    public static <V, E> Map<V, EgoMeasure<V>> calcule(final Graph<V, E> graph, 
            final Map<E, ? extends Number> edgeWeigth) {

        if (graph == null) {
            return new HashMap<>(1);
        }

        Transformer<E, ? extends Number> edgeWeigthTransformer = new Transformer<E, Number>() {
            @Override
            public Number transform(E edge) {
                return edgeWeigth.containsKey(edge) ? edgeWeigth.get(edge) : 0;
            }
        };
        
        final Map<V, Graph<V, E>> egoNetworks = 
                EgoNetworkExtractor.extractEgoNetwork(graph);
        
        final Map<V, EgoMeasure<V>> result = 
                new HashMap<>(graph.getVertexCount());
        
        for (V v : graph.getVertices()) {
            final Graph<V, E> egoNetwork = egoNetworks.get(v);
            
            final BetweennessCentrality<V, E> bc = 
                    new BetweennessCentrality<>(egoNetwork, edgeWeigthTransformer);
            
            result.put(v, new EgoMeasure<>(v, egoNetwork.getVertexCount(), 
                    egoNetwork.getEdgeCount(), bc.getVertexScore(v)));
        }
        
        return result;
    }
}
