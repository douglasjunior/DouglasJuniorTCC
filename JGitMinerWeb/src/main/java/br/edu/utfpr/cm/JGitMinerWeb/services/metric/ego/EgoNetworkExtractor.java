
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility for extract ego network from a graph.
 * 
 * @author Rodrigo T. Kuroda
 */
public class EgoNetworkExtractor {
    
    /**
     * Extracts all ego network of each vertex from graph.
     * 
     * @param <V> Vertex class
     * @param <E> Edge class
     * @param <G> Graph class
     * @param graph The graph where ego networks of vertices will be extracted
     * @return 
     */
    public static <V,E,G extends Graph<V,E>> Map<V, Graph<V, E>> extractEgoNetwork(G graph) {

        if (graph == null) {
            return new HashMap<>(1);
        }

        Map<V, Graph<V, E>> egoNetworks = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
            final Set<V> neighbors = new HashSet<>(graph.getNeighbors(v));
            neighbors.add(v); // add ego in his own ego network
            final Graph<V, E> egoNetwork = 
                    FilterUtils.createInducedSubgraph(neighbors, graph);
            egoNetworks.put(v, egoNetwork);
        }
        
        return egoNetworks;
    }
}
