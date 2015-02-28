package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author Rodrigo Kuroda
 */
public class FilePair {

    protected final String fileName;
    protected final String fileName2;

    public FilePair(String fileName, String fileName2) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileName2() {
        return fileName2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FilePair) {
            FilePair other = (FilePair) obj;
            if (Util.stringEquals(this.fileName, other.fileName)
                    && Util.stringEquals(this.fileName2, other.fileName2)) {
                return true;
            }
            if (Util.stringEquals(this.fileName, other.fileName2)
                    && Util.stringEquals(this.fileName2, other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash +
                (Objects.hashCode(this.fileName) + Objects.hashCode(this.fileName2));
        return hash;
    }

    /**
     * Prints the filename, filename2. The issueWeight and issues are printed
     * only if was added issues.
     *
     * @return String The attributes separated by semicolon (;)
     */
    @Override
    public String toString() {
        return fileName + ";" + fileName2 + ";";
    }
}
