package br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.Measure;
import java.util.Objects;

/**
 * Stores the structural holes measure of the vertex <code>V</code>.
 *
 * @author Rodrigo T. Kuroda
 * @param <V> Class of vertex
 */
public class StructuralHolesMeasure<V> extends Measure<V> {

    private final double efficiency;
    private final double effectiveSize;
    private final double constraint;
    private final double hierarchy;

    public StructuralHolesMeasure(V vertex,
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof StructuralHolesMeasure)) {
            return false;
        }

        final StructuralHolesMeasure<?> other = (StructuralHolesMeasure<?>) o;
        return Objects.equals(getVertex(), other.getVertex());
    }

    @Override
    public String toString() {
        return super.toString()
                + ", effective size: " + effectiveSize
                + ", efficiency: " + efficiency
                + ", constraint: " + constraint
                + ", hierarchy: " + hierarchy;
    }
}
