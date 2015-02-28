package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 * TODO add other metrics
 *
 * @author Rodrigo T. Kuroda
 */
public class CommitMetrics {

    public static final String HEADER = Commit.HEADER;

    private final Commit commit;

    public CommitMetrics(Commit commit) {
        this.commit = commit;
    }

    public Commit getCommit() {
        return commit;
    }

    @Override
    public String toString() {
        return commit.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.commit);
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
        final CommitMetrics other = (CommitMetrics) obj;
        if (!Objects.equals(this.commit, other.commit)) {
            return false;
        }
        return true;
    }

}
