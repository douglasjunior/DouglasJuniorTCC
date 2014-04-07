/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFileMetrics {

    private String file;
    private String file2;
    private List<Double> metrics = new ArrayList<>();

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

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile2() {
        return file2;
    }

    public void setFile2(String file2) {
        this.file2 = file2;
    }

    public List<Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Double> metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(file + ";" + file2);
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
        if (getClass() != obj.getClass()) {
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
