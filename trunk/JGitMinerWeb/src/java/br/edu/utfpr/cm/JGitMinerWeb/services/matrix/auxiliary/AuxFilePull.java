/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityPullRequest;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.fileName);
        hash = 89 * hash + Objects.hashCode(this.pull);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuxFilePull other = (AuxFilePull) obj;
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.pull, other.pull)) {
            return false;
        }
        return true;
    }
    
    
}
