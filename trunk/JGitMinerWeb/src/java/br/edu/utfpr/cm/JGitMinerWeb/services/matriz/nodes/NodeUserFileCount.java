/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.util.matriz.NodeConnection;

/**
 *
 * @author douglas
 */
public class NodeUserFileCount implements NodeConnection<EntityCommitUser, String> {

    private EntityCommitUser commitUser;
    private String fileName;
    private double weight;

    public NodeUserFileCount() {
    }

    public NodeUserFileCount(EntityCommitUser commitUser, String fileName, Long weight) {
        this.commitUser = commitUser;
        this.fileName = fileName;
        this.weight = weight;
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

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void incWeight() {
        weight++;
    }

    @Override
    public EntityCommitUser getFrom() {
        return commitUser;
    }

    @Override
    public String getTo() {
        return fileName;
    }
}
