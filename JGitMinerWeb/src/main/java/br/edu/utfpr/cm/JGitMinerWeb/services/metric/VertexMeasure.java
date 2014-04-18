package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

/**
 * Represents a measure of a vertex.
 * @author Rodrigo T. Kuroda <rodrigokuroda at gmail dot com>
 * @param <V> Class of vertex
 */
public class VertexMeasure<V> {
    
    private final V vertex;

    public VertexMeasure(V vertex) {
        this.vertex = vertex;
    }

    public V getVertex() {
        return vertex;
    }
}
