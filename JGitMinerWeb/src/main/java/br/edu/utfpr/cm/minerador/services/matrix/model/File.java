package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class File {

    private final String file;

    public File(String file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.file);
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
        final File other = (File) obj;
        if (Objects.equals(this.file, other.file)) {
            return true;
        }
        return false;
    }

    /**
     * Prints the filename, filename2. The issueWeight and issues are printed
     * only if was added issues.
     *
     * @return String The attributes separated by semicolon (;)
     */
    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append(file).append(';');
        return toString.toString();
    }

    public static String getToStringHeader() {
        return "file;";
    }
}
