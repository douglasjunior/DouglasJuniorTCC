package br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.VertexMeasure;
import java.util.Objects;

/**
 * Stores the structural holes measure.
 * 
 * @author Rodrigo T. Kuroda <rodrigokuroda at gmail dot com>
 * @param <V> Class of vertex
 */
public class StructuralHoleMeasure<V> extends VertexMeasure<V> {

    private final double efficiency;
    private final double effectiveSize;
    private final double constraint;
    private final double hierarchy;

    public StructuralHoleMeasure(V vertex,
            double efficiency, double effectiveSize,
            double constraint, double hierarchy) {
        super(vertex);
        this.efficiency = efficiency;
        this.effectiveSize = effectiveSize;
        this.constraint = constraint;
        this.hierarchy = hierarchy;
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
        hash = 89 * hash + Objects.hashCode(getVertex());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof StructuralHoleMeasure)) {
            return false;
        }
        
        final StructuralHoleMeasure<?> other = (StructuralHoleMeasure<?>) obj;
        return Objects.equals(getVertex(), other.getVertex());
    }
    
}
