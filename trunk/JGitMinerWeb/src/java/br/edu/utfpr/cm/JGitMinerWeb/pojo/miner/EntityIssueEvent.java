/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitIssueEvent")
@NamedQueries({
    @NamedQuery(name = "IssueEvent.findByURL", query = "SELECT e FROM EntityIssueEvent e WHERE e.url = :url"),
    @NamedQuery(name = "IssueEvent.findByEventIssueID", query = "SELECT e FROM EntityIssueEvent e WHERE e.idIssueEvent = :idIssueEvent")
})
public class EntityIssueEvent implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private EntityIssue issue;
    @Column(unique = true)
    private long idIssueEvent;
    @Column(columnDefinition = "text")
    private String commitId;
    @Column(columnDefinition = "text")
    private String event;
    @Column(columnDefinition = "text")
    private String url;
    @ManyToOne
    private EntityUser actor;

    public EntityIssueEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getMineredAt() {
        return mineredAt;
    }

    public void setMineredAt(Date mineredAt) {
        this.mineredAt = mineredAt;
    }

    public EntityUser getActor() {
        return actor;
    }

    public void setActor(EntityUser actor) {
        this.actor = actor;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getIdIssueEvent() {
        return idIssueEvent;
    }

    public void setIdIssueEvent(long idIssueEvent) {
        this.idIssueEvent = idIssueEvent;
    }

    public EntityIssue getIssue() {
        return issue;
    }

    public void setIssue(EntityIssue issue) {
        this.issue = issue;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        if (!(object instanceof EntityIssueEvent)) {
            return false;
        }
        EntityIssueEvent other = (EntityIssueEvent) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityIssueEvent[ id=" + id + " ]";
    }
}
