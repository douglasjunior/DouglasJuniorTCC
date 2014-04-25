package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.Measure;

/**
 * Stores the betweenness measure of the vertex <code>V</code>.
 * 
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class BetweennessMeasure<V> extends Measure<V> {

    private final double nodeBetweenness;
    
    public BetweennessMeasure(final V vertex, final double nodeBetweenness) {
        super(vertex);
        this.nodeBetweenness = nodeBetweenness;
    }

    public double getNodeBetweenness() {
        return nodeBetweenness;
    }

    @Override
    public String toString() {
        return super.toString() + 
                ", node betweenness centrality: " + nodeBetweenness;
    }
}
