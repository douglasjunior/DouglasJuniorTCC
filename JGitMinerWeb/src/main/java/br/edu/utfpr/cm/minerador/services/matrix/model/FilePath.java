package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 * Represents a file in a commit. To be equal, two objects needs to be in same
 * commitId and to have same fileId (duplicated file (ID) in same commit is not
 * possible).
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePath {

    public final Integer commitId;
    public final Integer fileId;
    public final String filePath;

    public FilePath(Integer commitId, Integer fileId, String filePath) {
        this.commitId = commitId;
        this.fileId = fileId;
        this.filePath = filePath;
    }

    public Integer getCommitId() {
        return commitId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.commitId);
        hash = 89 * hash + Objects.hashCode(this.fileId);
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
        final FilePath other = (FilePath) obj;
        return Objects.equals(this.commitId, other.commitId)
                && Objects.equals(this.fileId, other.fileId);
    }
}
