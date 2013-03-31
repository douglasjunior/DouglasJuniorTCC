/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitCommitFile",
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"repositoryCommit_id", "sha"})
})
@NamedQueries({
    @NamedQuery(name = "CommitFile.findByCommitAndSHA", query = "SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = :repoCommit AND f.sha = :sha ")
})
public class EntityCommitFile implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    private Integer additions;
    private Integer changes;
    private Integer deletions;
    @Column(columnDefinition = "text")
    private String blobUrl;
    @Column(columnDefinition = "text")
    private String filename;
    @Column(columnDefinition = "text")
    private String patch;
    @Column(columnDefinition = "text")
    private String rawUrl;
    @Column(columnDefinition = "text")
    private String sha;
    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repositoryCommit_id")
    private EntityRepositoryCommit repositoryCommit;

    public EntityCommitFile() {
        mineredAt = new Date();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAdditions() {
        return additions;
    }

    public void setAdditions(Integer additions) {
        this.additions = additions;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public Integer getChanges() {
        return changes;
    }

    public void setChanges(Integer changes) {
        this.changes = changes;
    }

    public Integer getDeletions() {
        return deletions;
    }

    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getMineredAt() {
        return mineredAt;
    }

    public void setMineredAt(Date mineredAt) {
        this.mineredAt = mineredAt;
    }

    public EntityRepositoryCommit getRepositoryCommit() {
        return repositoryCommit;
    }

    public void setRepositoryCommit(EntityRepositoryCommit repositoryCommit) {
        this.repositoryCommit = repositoryCommit;
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
        if (!(object instanceof EntityCommitFile)) {
            return false;
        }
        EntityCommitFile other = (EntityCommitFile) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityCommitFile[ id=" + id + " ]";
    }
}
