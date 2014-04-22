package br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes;

import edu.uci.ics.jung.algorithms.metrics.StructuralHoles;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 * Compute metrics of network structural holes. The metrics computed are
 * efficiency, effective size, constraint and hierarchy.
 *
 * To compute the Structural Holes using JUNG API, it is necessary the edges to
 * have a weight (or another metric).
 *
 * @author Rodrigo T. Kuroda
 */
public class StructuralHolesCalculator {

    /**
     * Calculates the structural holes metrics based in a Transformer that
     * returns the weight of given node.
     *
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @param edgeWeigthTransformer A Transformer that returns the weight for
     * each edge (E).
     * @return A Map where the the key is the vertex (V) and the value is a POJO
     * with result of metrics, named <code>StructuralHoleMeasure</code>.
     */
    public static <V, E> Map<V, StructuralHoleMeasure<V>> calculeStructuralHolesMetrics(
            final Graph<V, E> graph, final Transformer<E, ? extends Number> edgeWeigthTransformer) {

        StructuralHoles<V, E> structuralHoles
                = new StructuralHoles<>(graph, edgeWeigthTransformer);

        Map<V, StructuralHoleMeasure<V>> results
                = new HashMap<>(graph.getVertexCount());

        for (V vertex : graph.getVertices()) {
            double efficiency = structuralHoles.efficiency(vertex);
            double effectiveSize = structuralHoles.effectiveSize(vertex);
            double constraint = structuralHoles.constraint(vertex);
            double hierarchy = structuralHoles.hierarchy(vertex);

            StructuralHoleMeasure<V> metric
                    = new StructuralHoleMeasure<>(vertex,
                            efficiency, effectiveSize, constraint, hierarchy);
            results.put(vertex, metric);
        }

        return results;
    }

    /**
     * Calculates the structural holes metrics based in a Map with each edge
     * weight.
     *
     * @param <V> Vertex of the JUNG Graph
     * @param <E> Edge of the JUNG Graph
     * @param graph The built JUNG Graph
     * @param edgeWeigth A Map of weight for each edge (E).
     * @return A Map where the the key is the vertex (V) and the value is a POJO
     * with result of metrics, named <code>StructuralHoleMetric</code>.
     */
    public static <V, E> Map<V, StructuralHoleMeasure<V>> calculeStructuralHolesMetrics(
            final Graph<V, E> graph, final Map<E, ? extends Number> edgeWeigth) {

        Transformer<E, ? extends Number> edgeWeigthTransformer = new Transformer<E, Number>() {
            @Override
            public Number transform(E edge) {
                return edgeWeigth.containsKey(edge) ? edgeWeigth.get(edge) : 0;
            }
        };
        return calculeStructuralHolesMetrics(graph, edgeWeigthTransformer);
    }
}
