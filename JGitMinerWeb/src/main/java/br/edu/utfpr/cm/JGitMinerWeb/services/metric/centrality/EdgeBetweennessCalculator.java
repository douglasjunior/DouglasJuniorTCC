package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

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
    public static <V, E> Map<E, Double> calcule(final Graph<V, E> graph) {

        if (graph == null) {
            return new HashMap<>(1);
        }

        BetweennessCentrality<V, E> bc = new BetweennessCentrality<>(graph);
        
        Map<E, Double> result = new HashMap<>(graph.getVertexCount());
        for (E e : graph.getEdges()) {
            result.put(e, bc.getEdgeScore(e));
        }
        
        return result;
    }
    
    /**
     * Calculates betweenness centrality measure for each edge <code>E</code> 
     * of a graph <code>G</code>.
     * 
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @param edgeWeigth A Map of weight for each edge (E).
     * @return A Map where the the key is the edge (E) and the value is 
     *      a POJO with result of metrics, named <code>EdgeBetweennessMeasure</code>.
     */
    public static <V, E> Map<E, Double> calcule(final Graph<V, E> graph, 
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
        
        BetweennessCentrality<V, E> bc = new BetweennessCentrality<>(graph, edgeWeigthTransformer);
        
        Map<E, Double> result = new HashMap<>(graph.getVertexCount());
        for (E e : graph.getEdges()) {
            result.put(e, bc.getEdgeScore(e));
        }
        
        return result;
    }
}
