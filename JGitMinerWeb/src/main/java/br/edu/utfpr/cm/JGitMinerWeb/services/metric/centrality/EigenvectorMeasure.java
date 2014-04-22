package br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.VertexMeasure;

/**
 * Stores the eigenvector measure of the vertex <code>V</code>.
 * 
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class EigenvectorMeasure<V> extends VertexMeasure<V> {

    private final double eigenvector;
    
    public EigenvectorMeasure(V vertex, double eigenvector) {
        super(vertex);
        this.eigenvector = eigenvector;
    }

    public double getEigenvector() {
        return eigenvector;
    }

    @Override
    public String toString() {
        return super.toString() + ", eigenvector: " + eigenvector;
    }
}
