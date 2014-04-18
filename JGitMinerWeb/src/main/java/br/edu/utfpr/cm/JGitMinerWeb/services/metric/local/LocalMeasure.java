package br.edu.utfpr.cm.JGitMinerWeb.services.metric.local;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.VertexMeasure;

/**
 * Stores the local measure of vertex, that are:
 *  - in degree: incoming edges
 *  - out degree: outcoming edges
 *  - in and out degree: in degree + out degree
 * 
 * @author Rodrigo Takashi Kuroda
 */
public class LocalMeasure<V> extends VertexMeasure<V> {
    
    private final long inDegree;
    private final long outDegree;
    private final long inOutDegree;

    public LocalMeasure(V vertex, long inDegree, long outDegree) {
        super(vertex);
        this.inDegree = inDegree;
        this.outDegree = outDegree;
        this.inOutDegree = inDegree + outDegree;
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
}