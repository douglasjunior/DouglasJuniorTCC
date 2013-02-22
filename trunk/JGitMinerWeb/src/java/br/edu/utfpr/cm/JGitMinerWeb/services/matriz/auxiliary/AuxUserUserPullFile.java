/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;

/**
 *
 * @author douglas
 */
public class AuxUserUserPullFile {

    private EntityCommitUser commitUserX;
    private EntityCommitUser commitUserY;
    private EntityPullRequest pull;
    private String file;

    public AuxUserUserPullFile(EntityCommitUser commitUserX, EntityCommitUser commitUserY, EntityPullRequest pull, String file) {
        this.commitUserX = commitUserX;
        this.commitUserY = commitUserY;
        this.pull = pull;
        this.file = file;
    }

    public EntityCommitUser getCommitUserX() {
        return commitUserX;
    }

    public void setCommitUserX(EntityCommitUser commitUserX) {
        this.commitUserX = commitUserX;
    }

    public EntityCommitUser getCommitUserY() {
        return commitUserY;
    }

    public void setCommitUserY(EntityCommitUser commitUserY) {
        this.commitUserY = commitUserY;
    }

    public EntityPullRequest getPull() {
        return pull;
    }

    public void setPull(EntityPullRequest pull) {
        this.pull = pull;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUserPullFile) {
            AuxUserUserPullFile other = (AuxUserUserPullFile) obj;
            if (pull.equals(other.pull) && file.equals(other.file)) {
                if ((commitUserX.equals(other.commitUserX) && commitUserY.equals(other.commitUserY))
                        || (commitUserX.equals(other.commitUserY) && commitUserY.equals(other.commitUserX))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.commitUserX != null ? this.commitUserX.hashCode() : 0);
        hash = 71 * hash + (this.commitUserY != null ? this.commitUserY.hashCode() : 0);
        hash = 71 * hash + (this.pull != null ? this.pull.hashCode() : 0);
        return hash;
    }
}
