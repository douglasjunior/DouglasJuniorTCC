package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

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
     * @param edgeWeigth A Map of weight for each edge (E).
     * @return A Map where the the key is the vertex (V) and the value is 
     *      a POJO with result of metrics, named <code>EigenvectorMeasure</code>.
     */
    public static <V, E> Map<V, Double> calcule(final Graph<V, E> graph, 
            final Map<E, ? extends Number> edgeWeigth) {

        Transformer<E, ? extends Number> edgeWeigthTransformer = new Transformer<E, Number>() {
            @Override
            public Number transform(E edge) {
                return edgeWeigth.containsKey(edge) ? edgeWeigth.get(edge) : 0;
            }
        };
        
        EigenvectorCentrality<V, E> ec = new EigenvectorCentrality<>(graph, edgeWeigthTransformer);
        
        Map<V, Double> result = new HashMap<>(graph.getVertexCount());
        for (V v : graph.getVertices()) {
            result.put(v, ec.getVertexScore(v));
        }
        
        return result;
    }
}
