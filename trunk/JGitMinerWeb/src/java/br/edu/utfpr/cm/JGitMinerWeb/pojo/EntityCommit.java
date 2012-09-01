/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitCommit")
public class EntityCommit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @ManyToOne
    private EntityCommitUser author;
    @ManyToOne
    private EntityCommitUser committer;
    @OneToMany
    private List<EntityCommit> parents;
    @Column(columnDefinition = "text")
    private String message;
    private String sha;
    private String url;
    @ManyToOne
    private EntityTree tree;

    public EntityCommit() {
        mineredAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityCommitUser getAuthor() {
        return author;
    }

    public void setAuthor(EntityCommitUser author) {
        this.author = author;
    }

    public EntityCommitUser getCommitter() {
        return committer;
    }

    public void setCommitter(EntityCommitUser committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<EntityCommit> getParents() {
        return parents;
    }

    public void setParents(List<EntityCommit> parents) {
        this.parents = parents;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public EntityTree getTree() {
        return tree;
    }

    public void setTree(EntityTree tree) {
        this.tree = tree;
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
        if (!(object instanceof EntityCommit)) {
            return false;
        }
        EntityCommit other = (EntityCommit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityCommity[ id=" + id + " ]";
    }
}
