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
}
