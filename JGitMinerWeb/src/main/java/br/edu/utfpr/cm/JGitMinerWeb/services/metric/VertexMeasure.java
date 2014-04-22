package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import java.util.Objects;

/**
 * Represents a measure of a vertex.
 *
 * @author Rodrigo T. Kuroda <rodrigokuroda at gmail dot com>
 * @param <V> Class of vertex
 */
public class VertexMeasure<V> {

    private final V vertex;

    public VertexMeasure(V vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex can not be null.");
        }
        this.vertex = vertex;
    }

    public V getVertex() {
        return vertex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VertexMeasure<?> other = (VertexMeasure<?>) obj;
        return Objects.equals(this.vertex, other.vertex);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.vertex);
        return hash;
    }

    @Override
    public String toString() {
        return vertex.toString();
    }
}
