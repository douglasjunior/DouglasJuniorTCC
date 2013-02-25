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
public class AuxFileFilePull {
    
    private String fileName;
    private String fileName2;
    private EntityPullRequest pull;
    
    public AuxFileFilePull() {
    }
    
    public AuxFileFilePull(String fileName, String fileName2, EntityPullRequest pull) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
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
    
    public String getFileName2() {
        return fileName2;
    }
    
    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxFileFilePull) {
            AuxFileFilePull other = (AuxFileFilePull) obj;
            if (this.pull.equals(other.pull)) {
                if (this.fileName.equals(other.fileName)
                        && this.fileName2.equals(other.fileName2)) {
                    return true;
                }
                if (this.fileName.equals(other.fileName2)
                        && this.fileName2.equals(other.fileName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        hash = 67 * hash + (this.fileName2 != null ? this.fileName2.hashCode() : 0);
        hash = 67 * hash + (this.pull != null ? this.pull.hashCode() : 0);
        return hash;
    }
}
