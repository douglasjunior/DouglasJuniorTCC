package br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes;

import java.util.Objects;

/**
 *
 * @author a562273
 */
public class StructuralHoleMetric<V> {

    private final V vertex;
    private final double efficiency;
    private final double effectiveSize;
    private final double constraint;
    private final double hierarchy;

    public StructuralHoleMetric(V vertex,
            double efficiency, double effectiveSize,
            double constraint, double hierarchy) {
        this.vertex = vertex;
        this.efficiency = efficiency;
        this.effectiveSize = effectiveSize;
        this.constraint = constraint;
        this.hierarchy = hierarchy;
    }

    public V getVertex() {
        return vertex;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getEffectiveSize() {
        return effectiveSize;
    }

    public double getConstraint() {
        return constraint;
    }

    public double getHierarchy() {
        return hierarchy;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.vertex);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof StructuralHoleMetric)) {
            return false;
        }
        
        final StructuralHoleMetric<?> other = (StructuralHoleMetric<?>) obj;
        return Objects.equals(this.vertex, other.vertex);
    }
    
}
