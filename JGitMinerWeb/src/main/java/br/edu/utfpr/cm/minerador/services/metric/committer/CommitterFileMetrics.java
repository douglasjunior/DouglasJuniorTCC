package br.edu.utfpr.cm.minerador.services.metric.committer;

import br.edu.utfpr.cm.minerador.services.metric.model.File;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommitterFileMetrics {

    public final static String HEADER
            = "sameOwnership;" // 1 = mesmo autor que fez o ultimo commit do arquivo
            + "ownership;" // baseado no commit
            + "experience;" // baseado no code churn
            ;

    private final Committer committer;
    private final File file;
    private final double ownership;
    private final double experience;
    private final boolean sameOwnership;

    public CommitterFileMetrics(Committer committer, File file, boolean sameOwnership, double ownership, double experience) {
        this.committer = committer;
        this.file = file;
        this.ownership = ownership;
        this.experience = experience;
        this.sameOwnership = sameOwnership;
    }

    public Committer getCommitter() {
        return committer;
    }

    public File getFile() {
        return file;
    }

    public double getOwnership() {
        return ownership;
    }

    public double getExperience() {
        return experience;
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
        return BooleanUtils.toInteger(sameOwnership) + ";" + ownership + ";"
                + experience + ";";
    }
}
