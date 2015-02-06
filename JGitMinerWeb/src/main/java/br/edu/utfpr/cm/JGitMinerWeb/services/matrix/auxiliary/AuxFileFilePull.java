package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFilePull extends AuxFileFile {

    private Integer pullNumber;

    public AuxFileFilePull(String fileName, String fileName2, Integer pullNumber) {
        super(fileName, fileName2);
        this.pullNumber = pullNumber;
    }

    public Integer getPullNumber() {
        return pullNumber;
    }

    public void setPullNumber(Integer pullNumber) {
        this.pullNumber = pullNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof AuxFileFilePull) {
            AuxFileFilePull other = (AuxFileFilePull) obj;
            if (this.pullNumber.equals(other.pullNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (Objects.hashCode(this.fileName) + Objects.hashCode(this.fileName2));
        hash = 53 * hash + Objects.hashCode(this.pullNumber);
        return hash;
    }

    @Override
    public String toString() {
        return fileName + ";" + fileName2 + ";" + pullNumber;
    }

}
