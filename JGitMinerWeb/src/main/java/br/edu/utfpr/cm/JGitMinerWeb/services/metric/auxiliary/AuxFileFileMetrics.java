package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFileMetrics {

    private final String file;
    private final String file2;
    private final List<Double> metrics;

    public AuxFileFileMetrics(String file, String file2, double... metrics) {
        this.file = file;
        this.file2 = file2;
        this.metrics = new ArrayList<>();
        addMetrics(metrics);
    }
    
    public AuxFileFileMetrics(String file, String file2, List<Double> metrics) {
        this.file = file;
        this.file2 = file2;
        this.metrics = metrics;
    }

    public String getFile() {
        return file;
    }
    public String getFile2() {
        return file2;
    }

    public List<Double> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(file).append(";").append(file2);
        for (double m : metrics) {
            sb.append(";");
            sb.append(Util.tratarDoubleParaString(m));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (Objects.hashCode(this.file) + Objects.hashCode(this.file2));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AuxFileFileMetrics)) {
            return false;
        }
        final AuxFileFileMetrics other = (AuxFileFileMetrics) obj;
        if (Util.stringEquals(this.file, other.file) && Util.stringEquals(this.file2, other.file2)) {
            return true;
        }
        if (Util.stringEquals(this.file, other.file2) && Util.stringEquals(this.file2, other.file)) {
            return true;
        }
        return false;
    }

    public void addMetrics(double... metrics) {
        for (double value : metrics) {
            this.metrics.add(value);
        }
    }

}
