/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFilePull {

    private String fileName;
    private String fileName2;
    private Integer pullNumber;

    public AuxFileFilePull() {
    }

    public AuxFileFilePull(String fileName, String fileName2, Integer pullNumber) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.pullNumber = pullNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName2() {
        return fileName2;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }

    public Integer getPullNumber() {
        return pullNumber;
    }

    public void setPullNumber(Integer pullNumber) {
        this.pullNumber = pullNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxFileFilePull) {
            AuxFileFilePull other = (AuxFileFilePull) obj;
            if (this.pullNumber.equals(other.pullNumber)) {
                if (this.fileName.equals(other.fileName)
                        && this.fileName2.equals(other.fileName2)) {
                    return true;
                }
                if (this.fileName.equals(other.fileName2)
                        && this.fileName2.equals(other.fileName)) {
                    return true;
                }
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
