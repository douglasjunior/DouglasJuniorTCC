/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitRepositoryCommit")
@NamedQueries({
    @NamedQuery(name = "RepositoryCommit.findByURL", query = "SELECT c FROM EntityRepositoryCommit c WHERE c.url = :url")
})
public class EntityRepositoryCommit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @ManyToOne
    private EntityRepository repository;
    @ManyToOne
    private EntityCommit commit;
    @OneToOne
    private EntityCommitStats stats;
    @OneToMany
    @JoinTable(name="gitrepositorycommit_parents")
    private List<EntityCommit> parents;
    @OneToMany(mappedBy = "repositoryCommit")
    private List<EntityCommitFile> files;
    @Column(columnDefinition = "text")
    private String sha;
    private String url;
    @ManyToOne
    private EntityUser author;
    @ManyToOne
    private EntityUser committer; 

    public EntityRepositoryCommit() {
        parents = new ArrayList<EntityCommit>();
        files = new ArrayList<EntityCommitFile>();
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

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public EntityUser getAuthor() {
        return author;
    }

    public void setAuthor(EntityUser author) {
        this.author = author;
    }

    public EntityCommit getCommit() {
        return commit;
    }

    public void setCommit(EntityCommit commit) {
        this.commit = commit;
    }

    public EntityUser getCommitter() {
        return committer;
    }

    public void setCommitter(EntityUser committer) {
        this.committer = committer;
    }

    public List<EntityCommitFile> getFiles() {
        return files;
    }

    public void setFiles(List<EntityCommitFile> files) {
        this.files = files;
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

    public EntityCommitStats getStats() {
        return stats;
    }

    public void setStats(EntityCommitStats stats) {
        this.stats = stats;
        this.stats.setRepositoryCommit(this);
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
        if (!(object instanceof EntityRepositoryCommit)) {
            return false;
        }
        EntityRepositoryCommit other = (EntityRepositoryCommit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityRepositoryCommit[ id=" + id + " ]";
    }

    public void addParent(EntityCommit parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public void addFile(EntityCommitFile file) {
        if (!files.contains(file)) {
            files.add(file);
        }
        file.setRepositoryCommit(this);
    }
}
