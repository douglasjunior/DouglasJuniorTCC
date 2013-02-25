/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

/**
 *
 * @author douglas
 */
public class AuxFileFileCount {

    private String fileName;
    private String fileName2;
    private int count;

    public AuxFileFileCount() {
    }

    public AuxFileFileCount(String fileName, String fileName2) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.count = 1;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incCount() {
        count++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxFileFileCount) {
            AuxFileFileCount other = (AuxFileFileCount) obj;
            if (this.fileName.equals(other.fileName)
                    && this.fileName2.equals(other.fileName2)) {
                return true;
            }
            if (this.fileName.equals(other.fileName2)
                    && this.fileName2.equals(other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        hash = 53 * hash + (this.fileName2 != null ? this.fileName2.hashCode() : 0);
        return hash;
    }
}
