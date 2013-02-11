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
public class AuxUserUserPullFile {
    
    private EntityUser userX;
    private EntityUser userY;
    private EntityPullRequest pull;
    private String file;
    
    public AuxUserUserPullFile(EntityUser userX, EntityUser userY, EntityPullRequest pull, String file) {
        this.userX = userX;
        this.userY = userY;
        this.pull = pull;
        this.file = file;
    }
    
    public EntityUser getUserX() {
        return userX;
    }
    
    public void setUserX(EntityUser userX) {
        this.userX = userX;
    }
    
    public EntityUser getUserY() {
        return userY;
    }
    
    public void setUserY(EntityUser userY) {
        this.userY = userY;
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
                if ((userX.equals(other.userX) && userY.equals(other.userY))
                        || (userX.equals(other.userY) && userY.equals(other.userX))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.userX != null ? this.userX.hashCode() : 0);
        hash = 71 * hash + (this.userY != null ? this.userY.hashCode() : 0);
        hash = 71 * hash + (this.pull != null ? this.pull.hashCode() : 0);
        return hash;
    }
}
