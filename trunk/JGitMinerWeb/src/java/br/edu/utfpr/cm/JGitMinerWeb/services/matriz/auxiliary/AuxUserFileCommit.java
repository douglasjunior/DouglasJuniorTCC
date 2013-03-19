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
public class AuxUserFileCommit {

    private String user;
    private String fileName;
    private Long commit;

    public AuxUserFileCommit(String user, String fileName, Long commit) {
        this.user = user;
        this.fileName = fileName;
        this.commit = commit;
    }

    public Long getCommit() {
        return commit;
    }

    public void setCommit(Long commit) {
        this.commit = commit;
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserFileCommit) {
            AuxUserFileCommit other = (AuxUserFileCommit) obj;
            if (this.user.equals(other.user)
                    && this.fileName.equals(other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.user);
        hash = 83 * hash + Objects.hashCode(this.fileName);
        return hash;
    }
}
