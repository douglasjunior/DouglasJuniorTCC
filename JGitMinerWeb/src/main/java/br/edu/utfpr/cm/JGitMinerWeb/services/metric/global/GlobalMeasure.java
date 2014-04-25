package br.edu.utfpr.cm.JGitMinerWeb.services.metric.global;

/**
 * Stores the following local measure of the vertex <code>V</code>: - in degree:
 * incoming edges - out degree: outcoming edges - in and out degree: in degree +
 * out degree
 *
 * @author Rodrigo T. Kuroda
 */
public class GlobalMeasure {

    private final long size;
    private final long ties;
    private final double diameter;

    public GlobalMeasure(long size, long ties, double diameter) {
        this.size = size;
        this.ties = ties;
        this.diameter = diameter;
    }

    public long getSize() {
        return size;
    }

    public long getTies() {
        return ties;
    }

    public double getDiameter() {
        return diameter;
    }

    @Override
    public String toString() {
        return "size: " + size
                + ", ties: " + ties
                + ", diameter: " + diameter;
    }

}
