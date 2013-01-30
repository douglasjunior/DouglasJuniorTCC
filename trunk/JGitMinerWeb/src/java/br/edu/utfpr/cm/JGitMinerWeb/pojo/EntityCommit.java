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
@Table(name = "gitCommit")
@NamedQueries({
    @NamedQuery(name = "Commit.findByURL", query = "SELECT c FROM EntityCommit c WHERE c.url = :url")
})
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
//    @OneToMany(mappedBy = "son")
//    private List<EntityCommit> parents;
//    @ManyToOne
//    private EntityCommit son;
    @OneToMany(mappedBy = "commit")
    private List<EntityCommitComment> comments;
    @Column(columnDefinition = "text")
    private String message;
    private String sha;
    private String url;
    @ManyToOne
    private EntityTree tree;
    private int commentCount;

    public EntityCommit() {
//        parents = new ArrayList<EntityCommit>();
        comments = new ArrayList<EntityCommitComment>();
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

//    public List<EntityCommit> getParents() {
//        return parents;
//    }
//
//    public void setParents(List<EntityCommit> parents) {
//        this.parents = parents;
//    }
//
//    public EntityCommit getSon() {
//        return son;
//    }
//
//    public void setSon(EntityCommit son) {
//        this.son = son;
//    }
    public List<EntityCommitComment> getComments() {
        return comments;
    }

    public void setComments(List<EntityCommitComment> comments) {
        this.comments = comments;
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

//    public void addParent(EntityCommit parent) {
//        if (!parents.contains(parent)) {
//            parents.add(parent);
//        }
//        parent.setSon(this);
//    }
    public void addComment(EntityCommitComment comment) {
        if (!comments.contains(comment)) {
            comments.add(comment);
        }
        comment.setCommit(this);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getCommentCount() {
        return commentCount;
    }
}
