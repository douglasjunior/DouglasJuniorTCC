package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class FileIssueCommit extends FileIssue {

    private final Commit commit;

    public FileIssueCommit(String fileName, Integer issue, Commit commit) {
        super(fileName, issue);
        this.commit = commit;
    }

    public FileIssueCommit(File file, Integer issue, Commit commit) {
        super(file, issue);
        this.commit = commit;
    }

    public Commit getCommit() {
        return commit;
    }

    @Override
    public String toString() {
        return super.toString();
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
        if (super.equals(obj)) { // obj is instace of FileIssue too
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileIssueCommit other = (FileIssueCommit) obj;

            return Objects.equals(this.commit, other.commit);
        }
        return false;
    }
    
    
}
