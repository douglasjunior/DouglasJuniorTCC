package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class FileIssueCommit extends FileIssue {

    private final Integer commit;

    public FileIssueCommit(String fileName, Integer issue, Integer commit) {
        super(fileName, issue);
        this.commit = commit;
    }

    public FileIssueCommit(File file, Integer issue, Integer commit) {
        super(file, issue);
        this.commit = commit;
    }

    public Integer getCommit() {
        return commit;
    }

    @Override
    public String toString() {
        return super.toString() + ";" + commit;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + super.hashCode();
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
        final FileIssueCommit other = (FileIssueCommit) obj;

        if (super.equals(obj) // obj is instace of FileIssue too
                && Objects.equals(this.commit, other.commit)) {
            return true;
        }
        return true;
    }
    
    
}
