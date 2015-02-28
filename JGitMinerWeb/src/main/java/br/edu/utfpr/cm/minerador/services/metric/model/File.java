package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author Rodrigo Kuroda
 */
public class File {

    private final String fileName;

    public File(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof File) {
            File other = (File) obj;
            if (Util.stringEquals(this.fileName, other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (Objects.hashCode(this.fileName));
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
        return fileName + ";";
    }
}
