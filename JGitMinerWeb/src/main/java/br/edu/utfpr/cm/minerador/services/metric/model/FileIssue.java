package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class FileIssue {

    private final File file;
    private final Integer issue;

    public FileIssue(String fileName, Integer issue) {
        this.file = new File(fileName);
        this.issue = issue;
    }

    public FileIssue(File file, Integer issue) {
        this.file = file;
        this.issue = issue;
    }

    public File getFile() {
        return file;
    }

    public Integer getIssue() {
        return issue;
    }

    @Override
    public String toString() {
        return file.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.file);
        hash = 89 * hash + Objects.hashCode(this.issue);
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
        final FileIssue other = (FileIssue) obj;
        if (Objects.equals(this.file, other.file)
                && Objects.equals(this.issue, other.issue)) {
            return true;
        }
        return true;
    }
    
    
}
