/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "net")
@NamedQueries({
    @NamedQuery(name = "Net.findAllTheLatest", query = "SELECT n FROM EntityNet n ORDER BY n.netStart DESC")
})
public class EntityNet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date netStart;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date netStop;
    private boolean complete;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String netLog;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String netResult;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityRepository repository;

    public EntityNet() {
        netStart = new Date();
        complete = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getNetStart() {
        return netStart;
    }

    public void setNetStart(Date netStart) {
        this.netStart = netStart;
    }

    public Date getNetStop() {
        return netStop;
    }

    public void setNetStop(Date netStop) {
        this.netStop = netStop;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getNetLog() {
        return netLog;
    }

    public void setNetLog(String netLog) {
        this.netLog = netLog;
    }

    public String getNetResult() {
        return netResult;
    }

    public void setNetResult(String netResult) {
        this.netResult = netResult;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
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
        if (!(object instanceof EntityNet)) {
            return false;
        }
        EntityNet other = (EntityNet) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNet[ id=" + id + " ]";
    }
}
