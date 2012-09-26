/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "miner")
public class EntityMiner implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date minerStart;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date minerStop;
    private boolean complete;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String minerLog;

    public EntityMiner() {
        minerStart = new Date();
        complete = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMinerLog() {
        return minerLog;
    }

    public void setMinerLog(String minerLog) {
        this.minerLog = minerLog;
    }

    public Date getMinerStart() {
        return minerStart;
    }

    public void setMinerStart(Date minerStart) {
        this.minerStart = minerStart;
    }

    public Date getMinerStop() {
        return minerStop;
    }

    public void setMinerStop(Date minerStop) {
        this.minerStop = minerStop;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EntityMiner)) {
            return false;
        }
        EntityMiner other = (EntityMiner) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityMiner[ id=" + id + " ]";
    }
}
