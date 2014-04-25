package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import java.util.Objects;

/**
 * Identify measure of an element.
 *
 * @author Rodrigo T. Kuroda
 * @param <I> Class of identifier (usually vertex, edge, or graph)
 */
public class Measure<I> {

    private final I vertex;

    public Measure(I vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Identifier can not be null.");
        }
        this.vertex = vertex;
    }

    public I getVertex() {
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
        final Measure<?> other = (Measure<?>) obj;
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
