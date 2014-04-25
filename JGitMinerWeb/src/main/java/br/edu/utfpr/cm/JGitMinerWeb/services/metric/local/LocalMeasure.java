package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.VertexMeasure;

/**
 * Stores the following local measure of the vertex <code>V</code>: - in degree:
 * incoming edges - out degree: outcoming edges - in and out degree: in degree +
 * out degree
 *
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class LocalMeasure<V> extends VertexMeasure<V> {

    private final long inDegree;
    private final long outDegree;
    private final long inOutDegree;
    private final double diameter;

    public LocalMeasure(V vertex, long inDegree, long outDegree, double diameter) {
        super(vertex);
        this.inDegree = inDegree;
        this.outDegree = outDegree;
        this.inOutDegree = inDegree + outDegree;
        this.diameter = diameter;
    }

    public long getInDegree() {
        return inDegree;
    }

    public long getOutDegree() {
        return outDegree;
    }

    public long getInOutDegree() {
        return inOutDegree;
    }

    public double getDiameter() {
        return diameter;
    }

    @Override
    public String toString() {
        return super.toString()
                + ", in degree: " + inDegree
                + ", out degree: " + outDegree
                + ", in and out degree: " + inOutDegree
                + ", diameter: " + diameter;
    }

}
