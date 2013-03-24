/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

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
        this(
                userLogin == null || userLogin.isEmpty() ? userMail : userLogin,
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
            if (this.fileName.equals(other.fileName)) {
                if (this.user.equals(other.user) && this.user2.equals(other.user2)) {
                    return true;
                }
                if (this.user.equals(other.user2) && this.user2.equals(other.user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.user);
        hash = 79 * hash + Objects.hashCode(this.user2);
        hash = 79 * hash + Objects.hashCode(this.fileName);
        return hash;
    }

    @Override
    public String toString() {
        return user + ";" + user2 + ";" + fileName;
    }
}
