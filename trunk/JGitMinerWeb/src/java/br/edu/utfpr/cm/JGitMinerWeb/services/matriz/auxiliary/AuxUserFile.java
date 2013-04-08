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
public class AuxUserFile {

    private String user;
    private String file;

    public AuxUserFile(String user, String file) {
        this.user = user;
        this.file = file;
    }
    
    public AuxUserFile(String userLogin, String userEmail, String fileName) {
        if (userLogin == null || userLogin.isEmpty()) {
            this.user = userEmail;
        } else {
            this.user = userLogin;
        }
        this.file = fileName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserFile) {
            AuxUserFile other = (AuxUserFile) obj;
            if (this.user.equals(other.user)) {
                if (this.file.equals(other.file)) {
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
        hash = 79 * hash + Objects.hashCode(this.file);
        return hash;
    }
}
