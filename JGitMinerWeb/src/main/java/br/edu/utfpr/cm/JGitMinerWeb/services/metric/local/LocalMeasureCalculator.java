package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHoleMeasure;
import edu.uci.ics.jung.algorithms.metrics.StructuralHoles;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Rodrigo T. Kuroda <rodrigokuroda at gmail dot com>
 */
public class LocalMeasureCalculator {

    public static <V, E> Map<V, LocalMeasure<V>> calculeLocalMetrics(
            final Graph<V, E> graph) {

        Map<V, LocalMeasure<V>> results = new HashMap<>(graph.getVertexCount());

        for (V vertex : graph.getVertices()) {
            int inDegree = graph.inDegree(vertex);
            int outDegree = graph.outDegree(vertex);

            LocalMeasure<V> metric
                    = new LocalMeasure<>(vertex, inDegree, outDegree);
            results.put(vertex, metric);
        }

        return results;
    }
}
