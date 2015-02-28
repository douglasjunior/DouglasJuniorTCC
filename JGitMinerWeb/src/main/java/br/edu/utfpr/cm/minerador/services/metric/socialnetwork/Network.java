package br.edu.utfpr.cm.minerador.services.metric.socialnetwork;

import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import edu.uci.ics.jung.graph.Graph;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 * @param <V> vertice type
 * @param <E> edge type
 */
public class Network<V, E> {

    private final Graph<V, E> network;
    private final Map<V, Integer> edgesWeigth;
    private final List<Commenter> commenters;

    public Network(Graph<V, E> network, Map<V, Integer> edgesWeigth, List<Commenter> commenters) {
        this.network = network;
        this.edgesWeigth = edgesWeigth;
        this.commenters = commenters;
    }

    public Graph<V, E> getNetwork() {
        return network;
    }

    public Map<V, Integer> getEdgesWeigth() {
        return Collections.unmodifiableMap(edgesWeigth);
    }

    public List<Commenter> getCommenters() {
        return commenters;
    }

}
