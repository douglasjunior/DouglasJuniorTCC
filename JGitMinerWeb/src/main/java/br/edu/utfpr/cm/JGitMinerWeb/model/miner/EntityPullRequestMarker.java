/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.model.miner;

import br.edu.utfpr.cm.JGitMinerWeb.model.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitPullRequestMaker", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"refpullrequestmarker", "repo_id", "user_id"})
}, indexes = {
    @Index(columnList = "refpullrequestmarker,repo_id,user_id", unique = true),
    @Index(columnList = "repo_id"),
    @Index(columnList = "user_id")
})
@NamedQueries({
    @NamedQuery(name = "PullRequestMarker.findByRefRepoUser",
            query = "SELECT p FROM EntityPullRequestMarker p "
            + " WHERE p.user = :user "
            + " AND p.repo = :repo "
            + " AND p.refPullRequestMarker = :ref ")
})
public class EntityPullRequestMarker implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private EntityRepository repo;
    @Column(length = 255)
    private String label;
    @Column(length = 255,name = "refpullrequestmarker")
    private String refPullRequestMarker;
    @Column(length = 255)
    private String sha;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private EntityUser user;

    public EntityPullRequestMarker() {
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRefPullRequestMarker() {
        return refPullRequestMarker;
    }

    public void setRefPullRequestMarker(String refPullRequestMarker) {
        this.refPullRequestMarker = refPullRequestMarker;
    }

    public EntityRepository getRepo() {
        return repo;
    }

    public void setRepo(EntityRepository repo) {
        this.repo = repo;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public EntityUser getUser() {
        return user;
    }

    public void setUser(EntityUser user) {
        this.user = user;
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
        if (!(object instanceof EntityPullRequestMarker)) {
            return false;
        }
        EntityPullRequestMarker other = (EntityPullRequestMarker) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityPullRequestMarker[ id=" + id + " ]";
    }
}
