package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class LocalMeasureCalculator {

    public static <V, E> Map<V, LocalMeasure<V>> calculeLocalMetrics(
            final Graph<V, E> graph) {

        Map<V, LocalMeasure<V>> results = new HashMap<>(graph.getVertexCount());

        for (V vertex : graph.getVertices()) {
            int inDegree = graph.inDegree(vertex);
            int outDegree = graph.outDegree(vertex);
            double diameter = DistanceStatistics.diameter(graph);

            LocalMeasure<V> metric
                    = new LocalMeasure<>(vertex, inDegree, outDegree, diameter);
            results.put(vertex, metric);
        }

        return results;
    }
}
