/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitCommitComment")
@NamedQueries({
    @NamedQuery(name = "CommitComment.findByURL", query = "SELECT c FROM EntityCommitComment c WHERE c.url = :url")
})
public class EntityCommitComment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(columnDefinition = "text")
    private String body;
    @Column(columnDefinition = "text")
    private String bodyHtml;
    @Column(columnDefinition = "text")
    private String bodyText;
    @Column(unique = true)
    private long idComment;
    private String url;
    private int line;
    private int position;
    @Column(columnDefinition = "text")
    private String commitId;
    @Column(columnDefinition = "text")
    private String pathCommitComment;
    @ManyToOne
    private EntityCommit commit;
    @ManyToOne
    private EntityUser user;

    public EntityCommitComment() {
        mineredAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getPathCommitComment() {
        return pathCommitComment;
    }

    public void setPathCommitComment(String pathCommitComment) {
        this.pathCommitComment = pathCommitComment;
    }

    public EntityCommit getCommit() {
        return commit;
    }

    public void setCommit(EntityCommit commit) {
        this.commit = commit;
    }

    public EntityUser getUser() {
        return user;
    }

    public void setUser(EntityUser user) {
        this.user = user;
    }

    public Date getMineredAt() {
        return mineredAt;
    }

    public void setMineredAt(Date mineredAt) {
        this.mineredAt = mineredAt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getIdComment() {
        return idComment;
    }

    public void setIdComment(long idComment) {
        this.idComment = idComment;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
        if (!(object instanceof EntityCommitComment)) {
            return false;
        }
        EntityCommitComment other = (EntityCommitComment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityCommitComment[ id=" + id + " ]";
    }
}
