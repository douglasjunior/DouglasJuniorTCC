package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 * A file pair in issue.
 *
 * @author Rodrigo Kuroda
 */
public class FilePairIssue {

    private final FilePair fileFile;
    private Integer issue;

    public FilePairIssue(String fileName, String fileName2, Integer issue) {
        fileFile = new FilePair(fileName, fileName2);
        this.issue = issue;
    }

    public FilePairIssue(FilePair fileFile, Integer issue) {
        this.fileFile = fileFile;
        this.issue = issue;
    }

    public FilePair getFileFile() {
        return fileFile;
    }

    public String getFileName() {
        return fileFile.getFileName();
    }

    public String getFileName2() {
        return fileFile.getFileName2();
    }

    public Integer getIssue() {
        return issue;
    }

    public void setIssue(Integer issue) {
        this.issue = issue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FilePairIssue) {
            FilePairIssue other = (FilePairIssue) obj;
            if (fileFile.equals(other.getFileFile())
                    && this.issue.equals(other.issue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (Objects.hashCode(fileFile.getFileName()) + Objects.hashCode(fileFile.getFileName2()));
        hash = 53 * hash + Objects.hashCode(this.issue);
        return hash;
    }

    @Override
    public String toString() {
        return fileFile.getFileName() + ";" + fileFile.getFileName2() + ";" + issue;
    }

}
