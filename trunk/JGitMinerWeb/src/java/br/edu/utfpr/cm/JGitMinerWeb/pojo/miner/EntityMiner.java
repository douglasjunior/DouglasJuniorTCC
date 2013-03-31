/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.Startable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "miner")
@NamedQueries({
    @NamedQuery(name = "Miner.findAllTheLatest", query = "SELECT m FROM EntityMiner m ORDER BY m.started DESC")
})
public class EntityMiner implements InterfaceEntity, Startable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date started;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date stoped;
    private boolean complete;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String log;
    @ManyToOne
    private EntityRepository repository;

    public EntityMiner() {
        started = new Date();
        complete = false;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getStoped() {
        return stoped;
    }

    public void setStoped(Date stoped) {
        this.stoped = stoped;
    }

    @Override
    public String getLog() {
        return log;
    }

    @Override
    public void setLog(String log) {
        this.log = log;
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

    @Override
    public String getDownloadFileName() {
        return this.repository + "-" + this.started;
    }
}
