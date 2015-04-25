package br.edu.utfpr.cm.minerador.services.metric.committer;

import br.edu.utfpr.cm.minerador.services.metric.model.File;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommitterFileMetrics {

    public final static String HEADER
            = "pv_ownership;" // baseado no commit
            + "pv_experience;" // baseado no code churn
            ;

    private final Committer committer;
    private final File file;
    private final double ownership;
    private final double experience;

    public CommitterFileMetrics(Committer committer, File file, double ownership, double experience) {
        this.committer = committer;
        this.file = file;
        this.ownership = ownership;
        this.experience = experience;
    }

    public Committer getCommitter() {
        return committer;
    }

    public File getFile() {
        return file;
    }

    public double getOwnership() {
        return ownership == Double.NaN ? 0 : ownership;
    }

    public double getExperience() {
        return experience == Double.NaN ? 0 : experience;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.committer);
        hash = 59 * hash + Objects.hashCode(this.file);
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
        final CommitterFileMetrics other = (CommitterFileMetrics) obj;
        if (!Objects.equals(this.committer, other.committer)) {
            return false;
        }
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (ownership == Double.NaN ? 0.0d : ownership) + ";"
                + (experience == Double.NaN ? 0.0d : experience) + ";";
    }
}
