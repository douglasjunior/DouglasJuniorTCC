package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.Measure;

/**
 * Stores the betweenness measure of the edge <code>V</code>.
 * 
 * @author Rodrigo T. Kuroda
 * @param <V> Class of edge
 */
public class EdgeBetweennessMeasure<E> extends Measure<E> {

    private final double edgeBetweenness;
    
    public EdgeBetweennessMeasure(final E edge, final double edgeBetweenness) {
        super(edge);
        this.edgeBetweenness = edgeBetweenness;
    }

    public double getEdgeBetweenness() {
        return edgeBetweenness;
    }

    @Override
    public String toString() {
        return super.toString() + ", edge betweenness centrality: " + edgeBetweenness;
    }
}
