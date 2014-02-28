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
public class AuxUserFileFile {

    private String user;
    private String fileName;
    private String fileName2;
    private int weight;

    public AuxUserFileFile(String user, String fileName, String fileName2) {
        this.user = user;
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.weight = 1;
    }

    public AuxUserFileFile(String userLogin, String userMail, String fileName, String fileName2) {
        this(userLogin == null || userLogin.isEmpty() ? userMail : userLogin,
                fileName, fileName2);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public int inc() {
        return weight++;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserFileFile) {
            AuxUserFileFile other = (AuxUserFileFile) obj;
            // os arquivos não tem direção
            if (Util.stringEquals(this.user, other.user)) {
                return fileEquals(other);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.user);
        hash = 23 * hash + Objects.hashCode(this.fileName);
        hash = 23 * hash + Objects.hashCode(this.fileName2);
        return hash;
    }

    @Override
    public String toString() {
        return user + ";" + fileName + ";" + fileName2 + ";" + weight;
    }

    public boolean fileEquals(AuxUserFileFile other) {
        if ((Util.stringEquals(this.fileName, other.fileName) && Util.stringEquals(this.fileName2, other.fileName2))
                || (Util.stringEquals(this.fileName, other.fileName2) && Util.stringEquals(this.fileName2, other.fileName))) {
            return true;
        }
        return false;
    }
}
