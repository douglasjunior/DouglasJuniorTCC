/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequest;

/**
 *
 * @author douglas
 */
public class AuxFilePull {

    private String fileName;
    private EntityPullRequest pull;

    public AuxFilePull() {
    }

    public AuxFilePull(String fileName, EntityPullRequest pull) {
        this.fileName = fileName;
        this.pull = pull;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public EntityPullRequest getPull() {
        return pull;
    }

    public void setPull(EntityPullRequest pull) {
        this.pull = pull;
    }
}
