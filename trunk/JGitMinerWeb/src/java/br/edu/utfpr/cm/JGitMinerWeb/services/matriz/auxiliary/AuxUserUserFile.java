/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;

/**
 *
 * @author douglas
 */
public class AuxUserUserFile {

    private EntityCommitUser commitUser;
    private EntityCommitUser commitUser2;
    private String file;

    public AuxUserUserFile(EntityCommitUser commitUser, EntityCommitUser commitUser2, String file) {
        this.commitUser = commitUser;
        this.commitUser2 = commitUser2;
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public EntityCommitUser getCommitUser() {
        return commitUser;
    }

    public void setCommitUser(EntityCommitUser commitUser) {
        this.commitUser = commitUser;
    }

    public EntityCommitUser getCommitUser2() {
        return commitUser2;
    }

    public void setCommitUser2(EntityCommitUser commitUser2) {
        this.commitUser2 = commitUser2;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUserFile) {
            AuxUserUserFile other = (AuxUserUserFile) obj;
            if (this.file.equals(other.file)) {
                if (this.commitUser.equals(other.commitUser) && this.commitUser2.equals(other.commitUser2)) {
                    return true;
                }
                if (this.commitUser.equals(other.commitUser2) && this.commitUser2.equals(other.commitUser)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.commitUser != null ? this.commitUser.hashCode() : 0);
        hash += (this.commitUser2 != null ? this.commitUser2.hashCode() : 0);
        hash += (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return commitUser + " | " + commitUser2 + " | " + file;
    }
}
