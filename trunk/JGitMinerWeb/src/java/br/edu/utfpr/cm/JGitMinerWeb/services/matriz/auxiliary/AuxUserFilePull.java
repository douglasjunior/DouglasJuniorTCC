/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;

/**
 *
 * @author douglas
 */
public class AuxUserFilePull {

    private EntityUser user;
    private EntityPullRequest pull;
    private String file;

    public AuxUserFilePull() {
    }

    public AuxUserFilePull(EntityUser user, EntityPullRequest pull, String file) {
        this.user = user;
        this.pull = pull;
        this.file = file;
    }

    public EntityUser getUser() {
        return user;
    }

    public void setUser(EntityUser user) {
        this.user = user;
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
