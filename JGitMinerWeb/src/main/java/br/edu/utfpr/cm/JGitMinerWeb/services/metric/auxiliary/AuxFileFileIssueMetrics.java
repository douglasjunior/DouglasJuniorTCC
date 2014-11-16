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
public class AuxFileFileIssueMetrics {

    private final Integer issue;
    private final String file;
    private final String file2;
    private final List<Double> metrics;

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        this.file = file;
        this.file2 = file2;
        this.metrics = new ArrayList<>();
        this.issue = issue;
        addMetrics(metrics);
    }

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        this.file = file;
        this.file2 = file2;
        this.metrics = metrics;
        this.issue = issue;
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
        StringBuilder sb = new StringBuilder(getFile());
        sb.append(";").append(getFile2()).append(";").append(issue);
        for (double m : getMetrics()) {
            sb.append(";");
            sb.append(Util.tratarDoubleParaString(m));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (Objects.hashCode(this.file) + Objects.hashCode(this.file2));
        hash = 89 * hash + Objects.hashCode(this.issue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AuxFileFileIssueMetrics)) {
            return false;
        }
        final AuxFileFileIssueMetrics other = (AuxFileFileIssueMetrics) obj;
        if (Objects.equals(issue, other.issue)) {
            if (Util.stringEquals(this.file, other.file)
                    && Util.stringEquals(this.file2, other.file2)) {
                return true;
            }
            if (Util.stringEquals(this.file, other.file2)
                    && Util.stringEquals(this.file2, other.file)) {
                return true;
            }
        }
        return false;
    }

    public void addMetrics(double... metrics) {
        for (double value : metrics) {
            this.metrics.add(value);
        }
    }
}
