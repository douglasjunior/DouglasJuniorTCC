package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.VertexMeasure;

/**
 * Stores the eigenvector measure of the vertex <code>V</code>.
 * 
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class EgoMeasure<V> extends VertexMeasure<V> {

    private final long size;
    private final long ties;
    
    public EgoMeasure(V vertex, long size, long ties) {
        super(vertex);
        this.size = size;
        this.ties = ties;
    }

    public long getSize() {
        return size;
    }

    public long getTies() {
        return ties;
    }

    @Override
    public String toString() {
        return super.toString() + ", size: " + size + ", ties: " + ties;
    }
}
