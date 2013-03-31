/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitRepositoryCommit")
@NamedQueries({
    @NamedQuery(name = "RepositoryCommit.findByURL", query = "SELECT c FROM EntityRepositoryCommit c WHERE c.url = :url"),
    @NamedQuery(name = "RepositoryCommit.findBySHA", query = "SELECT c FROM EntityRepositoryCommit c WHERE c.sha = :sha")
})
public class EntityRepositoryCommit implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityRepository repository;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityCommit commit;
    @OneToOne(fetch = FetchType.LAZY)
    private EntityCommitStats stats;
    @OneToMany(mappedBy = "repositoryCommit", fetch = FetchType.LAZY)
    private Set<EntityCommitFile> files;
    @Column(columnDefinition = "text", unique = true)
    private String sha;
    private String url;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityUser author;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityUser committer;
    @OneToMany(mappedBy = "repositoryCommit", fetch = FetchType.LAZY)
    private Set<EntityCommitComment> comments;

    public EntityRepositoryCommit() {
        comments = new HashSet<EntityCommitComment>();
        files = new HashSet<EntityCommitFile>();
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

    public Set<EntityCommitComment> getComments() {
        return comments;
    }

    public void setComments(Set<EntityCommitComment> comments) {
        this.comments = comments;
    }

    public Set<EntityCommitFile> getFiles() {
        return files;
    }

    public void setFiles(Set<EntityCommitFile> files) {
        this.files = files;
    }

//    public List<EntityCommit> getParents() {
//        return parents;
//    }
//
//    public void setParents(List<EntityCommit> parents) {
//        this.parents = parents;
//    }
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

//    public void addParent(EntityCommit parent) {
//        if (!parents.contains(parent)) {
//            parents.add(parent);
//        }
//    }
    public void addFile(EntityCommitFile file) {
        files.add(file);
        file.setRepositoryCommit(this);
    }

    public void addComment(EntityCommitComment comment) {
        comments.add(comment);
        comment.setRepositoryCommit(this);
    }
}
