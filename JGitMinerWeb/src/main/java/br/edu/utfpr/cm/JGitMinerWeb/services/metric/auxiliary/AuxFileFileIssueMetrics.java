package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFileIssueMetrics extends AuxFileFileMetrics {

    private final Integer issue;

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        super(file, file2, metrics);
        this.issue = issue;
    }

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        super(file, file2, metrics);
        this.issue = issue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(";").append(issue);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + (Objects.hashCode(this.issue) + Objects.hashCode(this.issue));
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
        if (issue != null && this.issue.equals(other.issue)) {
            return super.equals(obj);
        }
        return false;
    }

}
