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
    private final long pairs;
    private final double density;
    private final double diameter;

    public GlobalMeasure(long size, long ties, double diameter) {
        this.size = size;
        this.ties = ties;
        this.pairs = size * (size - 1);
        this.density = pairs == 0 ? 1 : (double) ties / (double) pairs;
        this.diameter = diameter;
    }

    public long getSize() {
        return size;
    }

    public long getTies() {
        return ties;
    }

    public double getDensity() {
        return density;
    }

    public long getPairs() {
        return pairs;
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
