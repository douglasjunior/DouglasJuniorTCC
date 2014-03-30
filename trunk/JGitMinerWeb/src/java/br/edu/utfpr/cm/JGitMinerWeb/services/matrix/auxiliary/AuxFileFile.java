/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFile {

    private String fileName;
    private String fileName2;

    public AuxFileFile() {
    }

    public AuxFileFile(String fileName, String fileName2) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxFileFile) {
            AuxFileFile other = (AuxFileFile) obj;
            if (Util.stringEquals(this.fileName, other.fileName)
                    && Util.stringEquals(this.fileName2, other.fileName2)) {
                return true;
            }
            if (Util.stringEquals(this.fileName, other.fileName2)
                    && Util.stringEquals(this.fileName2, other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.fileName)+ Objects.hashCode(this.fileName2);
        return hash;
    }

}
