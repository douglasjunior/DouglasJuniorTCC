/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes;

import br.edu.utfpr.cm.JGitMinerWeb.util.matriz.NodeConnection;

/**
 *
 * @author douglas
 */
public class NodeFileFileCount implements NodeConnection<String, String> {

    private String fileName;
    private String fileName2;
    private double weight;

    public NodeFileFileCount() {
    }

    public NodeFileFileCount(String fileName, String fileName2) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.weight = 1;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName2() {
        return fileName2;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
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
    public String getFrom() {
        return fileName;
    }

    @Override
    public String getTo() {
        return fileName2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof NodeFileFileCount) {
            NodeFileFileCount other = (NodeFileFileCount) obj;
            if (this.fileName.equals(other.fileName)
                    && this.fileName2.equals(other.fileName2)) {
                return true;
            }
            if (this.fileName.equals(other.fileName2)
                    && this.fileName2.equals(other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        hash = 53 * hash + (this.fileName2 != null ? this.fileName2.hashCode() : 0);
        return hash;
    }
}
