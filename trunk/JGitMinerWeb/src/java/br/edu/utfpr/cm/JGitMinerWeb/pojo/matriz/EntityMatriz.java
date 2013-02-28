/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "matriz")
@NamedQueries({
    @NamedQuery(name = "Matriz.findAllTheLatest", query = "SELECT m FROM EntityMatriz m ORDER BY m.started DESC")
})
public class EntityMatriz implements InterfaceEntity, Serializable {

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
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityRepository repository;
    @OneToMany(mappedBy = "matriz", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<EntityMatrizRecord> records;
    private String classServicesName;

    public EntityMatriz() {
        started = new Date();
        complete = false;
        records = new HashSet<EntityMatrizRecord>();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Set<EntityMatrizRecord> getRecords() {
        return records;
    }

    public void setRecords(Set<EntityMatrizRecord> records) {
        this.records = records;
    }

    public String getClassServicesName() {
        return classServicesName;
    }

    public String getClassServicesSingleName() {
        if (classServicesName != null) {
            String[] tokens = classServicesName.split("\\.");
            if (tokens.length > 0) {
                return tokens[tokens.length - 1];
            }
        }
        return classServicesName;
    }

    public void setClassServicesName(String classServices) {
        this.classServicesName = classServices;
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
        if (!(object instanceof EntityMatriz)) {
            return false;
        }
        EntityMatriz other = (EntityMatriz) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNet[ id=" + id + " ]";
    }

    public void addRecord(EntityMatrizRecord rec) {
       records.add(rec);
       rec.setMatriz(this);
    }
}
