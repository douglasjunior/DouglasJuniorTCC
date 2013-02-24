/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommit;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;

/**
 *
 * @author douglas
 */
public class AuxUserFileCount {

    private EntityCommitUser commitUser;
    private String fileName;
    private Long count;

    public AuxUserFileCount() {
    }

    public AuxUserFileCount(EntityCommitUser commitUser, String fileName, Long count) {
        this.commitUser = commitUser;
        this.fileName = fileName;
        this.count = count;
    }

    public EntityCommitUser getCommitUser() {
        return commitUser;
    }

    public void setCommitUser(EntityCommitUser commitUser) {
        this.commitUser = commitUser;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AuxUserFileCount)) {
            return false;
        }
        AuxUserFileCount other = (AuxUserFileCount) obj;
        if (!this.commitUser.equals(other.commitUser)
                || !this.fileName.equals(other.fileName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.commitUser != null ? this.commitUser.hashCode() : 0);
        hash = 89 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        return hash;
    }
}
