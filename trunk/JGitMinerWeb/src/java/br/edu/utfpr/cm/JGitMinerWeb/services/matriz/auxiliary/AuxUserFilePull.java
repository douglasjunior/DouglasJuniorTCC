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
public class AuxUserFilePull {

    private EntityCommitUser commitUser;
    private EntityPullRequest pull;
    private String file;

    public AuxUserFilePull() {
    }

    public AuxUserFilePull(EntityCommitUser commitUser, EntityPullRequest pull, String file) {
        this.commitUser = commitUser;
        this.pull = pull;
        this.file = file;
    }

    public EntityCommitUser getCommitUser() {
        return commitUser;
    }

    public void setCommitUser(EntityCommitUser commitUser) {
        this.commitUser = commitUser;
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
}
