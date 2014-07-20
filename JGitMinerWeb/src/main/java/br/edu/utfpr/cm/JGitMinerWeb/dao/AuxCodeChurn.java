package br.edu.utfpr.cm.JGitMinerWeb.dao;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class AuxCodeChurn {

    private final String file;
    private final String file2;
    private final long additions;
    private final long deletions;
    private final long changes;

    public AuxCodeChurn(String file, long additions, long deletions, long changes) {
        this.file = file;
        this.file2 = null;
        this.additions = additions;
        this.deletions = deletions;
        this.changes = changes;
    }

    public AuxCodeChurn(String file, String file2, long additions, long deletions, long changes) {
        this.file = file;
        this.file2 = file2;
        this.additions = additions;
        this.deletions = deletions;
        this.changes = changes;
    }

    public AuxCodeChurn(long additions, long deletions, long changes) {
        this.file = null;
        this.file2 = null;
        this.additions = additions;
        this.deletions = deletions;
        this.changes = changes;
    }

    public String getFile() {
        return file;
    }

    public String getFile2() {
        return file2;
    }

    public long getAdditions() {
        return additions;
    }

    public long getDeletions() {
        return deletions;
    }

    public double getAdditionsNormalized() {
        return (double) additions / (double) changes;
    }

    public double getDeletionsNormalized() {
        return (double) deletions / (double) changes;
    }

    public long getChanges() {
        return changes;
    }
}
