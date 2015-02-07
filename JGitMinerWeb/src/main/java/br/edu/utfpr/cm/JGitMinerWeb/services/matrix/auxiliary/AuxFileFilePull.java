package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFilePull {

    private final AuxFileFile fileFile;
    private Integer pullNumber;

    public AuxFileFilePull(String fileName, String fileName2, Integer pullNumber) {
        fileFile = new AuxFileFile(fileName, fileName2);
        this.pullNumber = pullNumber;
    }

    public AuxFileFile getFileFile() {
        return fileFile;
    }

    public String getFileName() {
        return fileFile.getFileName();
    }

    public String getFileName2() {
        return fileFile.getFileName2();
    }

    public Integer getPullNumber() {
        return pullNumber;
    }

    public void setPullNumber(Integer pullNumber) {
        this.pullNumber = pullNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuxFileFilePull) {
            AuxFileFilePull other = (AuxFileFilePull) obj;
            if (fileFile.equals(other.getFileFile())
                    && this.pullNumber.equals(other.pullNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (Objects.hashCode(fileFile.getFileName()) + Objects.hashCode(fileFile.getFileName2()));
        hash = 53 * hash + Objects.hashCode(this.pullNumber);
        return hash;
    }

    @Override
    public String toString() {
        return fileFile.getFileName() + ";" + fileFile.getFileName2() + ";" + pullNumber;
    }

}
