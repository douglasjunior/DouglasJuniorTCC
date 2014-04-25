package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 * Measures properties of each node from the graph (in, out, in out degree, 
 * and clustering coefficient).
 * 
 * @author Rodrigo T. Kuroda
 */
public class LocalMeasureCalculator {

    public static <V, E> Map<V, LocalMeasure<V>> calculeLocalMetrics(
            final Graph<V, E> graph) {

        Map<V, LocalMeasure<V>> results = new HashMap<>(graph.getVertexCount());
        
        Map<V, Double> clusteringCoefficients = Metrics.clusteringCoefficients(graph);
        for (V vertex : graph.getVertices()) {
            int inDegree = graph.inDegree(vertex);
            int outDegree = graph.outDegree(vertex);
            double clusteringCoefficient = clusteringCoefficients.get(vertex);
            LocalMeasure<V> metric
                    = new LocalMeasure<>(vertex, inDegree, outDegree, clusteringCoefficient);
            results.put(vertex, metric);
        }

        return results;
    }
}
