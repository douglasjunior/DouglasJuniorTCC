package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Commit {

    public static final String HEADER = Committer.HEADER + "numFiles;";

    private final Integer id;
    private final Committer committer;
    private final Set<File> files;

    public Commit(Integer id, Committer commiter) {
        this.id = id;
        this.committer = commiter;
        this.files = new HashSet<>();
    }

    public Commit(Integer id, Committer commiter, Set<File> files) {
        this.id = id;
        this.committer = commiter;
        this.files = files;
    }

    public Integer getId() {
        return id;
    }

    public int getNumberOfFiles() {
        return files.size();
    }

    public Committer getCommiter() {
        return committer;
    }

    public Set<File> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return committer.toString() + getNumberOfFiles() + ";";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.id);
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
        final Commit other = (Commit) obj;
        return Objects.equals(this.id, other.id);
    }

}
