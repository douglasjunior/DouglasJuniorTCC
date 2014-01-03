/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxUserUserFile {

    private String user;
    private String user2;
    private String fileName;

    public AuxUserUserFile(String user, String user2, String fileName) {
        this.user = user;
        this.user2 = user2;
        this.fileName = fileName;
    }

    public AuxUserUserFile(String userLogin, String userMail, String userLogin2, String userMail2, String fileName) {
        this(userLogin == null || userLogin.isEmpty() ? userMail : userLogin,
                userLogin2 == null || userLogin2.isEmpty() ? userMail2 : userLogin2,
                fileName);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUserFile) {
            AuxUserUserFile other = (AuxUserUserFile) obj;
            if (Util.stringEquals(this.fileName, other.fileName)) {
                if (Util.stringEquals(this.user, other.user2)
                        && Util.stringEquals(this.user2, other.user)) {
                    return true;
                }
                if (Util.stringEquals(this.user, other.user)
                        && Util.stringEquals(this.user2, other.user2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.user);
        hash = 23 * hash + Objects.hashCode(this.user2);
        hash = 23 * hash + Objects.hashCode(this.fileName);
        return hash;
    }

    @Override
    public String toString() {
        return user + ";" + user2 + ";" + fileName;
    }
}
