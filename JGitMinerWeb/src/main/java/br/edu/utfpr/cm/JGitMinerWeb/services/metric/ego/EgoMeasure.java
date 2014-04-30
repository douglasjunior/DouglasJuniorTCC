package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.Measure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasure;

/**
 * Stores the eigenvector measure of the vertex <code>V</code>.
 * 
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class EgoMeasure<V> extends Measure<V> {

    private final GlobalMeasure globalMeasure;
    private final double betweennessCentrality;
    
    public EgoMeasure(final V vertex, final long size, final long ties, 
            final double betweennessCentrality) {
        super(vertex);
        this.globalMeasure = new GlobalMeasure(size, ties);
        this.betweennessCentrality = betweennessCentrality;
    }

    public long getSize() {
        return globalMeasure.getSize();
    }

    public long getTies() {
        return globalMeasure.getTies();
    }

    public long getPairs() {
        return globalMeasure.getPairs();
    }

    public double getDensity() {
        return globalMeasure.getDensity();
    }

    public double getBetweennessCentrality() {
        return betweennessCentrality;
    }

    @Override
    public String toString() {
        return super.toString() + ", size: " + getSize() + ", ties: " + getTies()
                + ", pairs: " + getPairs() + ", density: " + getDensity()
                + ", ego betweeness centrality: " + betweennessCentrality;
    }
}
