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
@Table(name = "gitIssue")
@NamedQueries({
    @NamedQuery(name = "Issue.findByIdIssue", query = "SELECT i FROM EntityIssue i WHERE i.idIssue = :idIssue"),
    @NamedQuery(name = "Issue.findByRepository", query = "SELECT i FROM EntityIssue i WHERE i.repository = :repository")
})
public class EntityIssue implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Column(unique = true)
    private long idIssue;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date closedAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date updatedAt;
    private int number;
    @OneToMany(cascade = CascadeType.ALL)
    private List<EntityLabel> labels;
    @ManyToOne(cascade = CascadeType.ALL)
    private EntityMilestone milestone;
    @OneToOne(cascade = CascadeType.ALL)
    private EntityPullRequest pullRequest;
    @Column(columnDefinition = "text")
    private String body;
    @Column(columnDefinition = "text")
    private String bodyHtml;
    @Column(columnDefinition = "text")
    private String bodyText;
    @Column(columnDefinition = "text")
    private String htmlUrl;
    private String stateIssue;
    @Column(columnDefinition = "text")
    private String title;
    @Column(columnDefinition = "text")
    private String url;
    @ManyToOne(cascade = CascadeType.ALL)
    private EntityUser assignee;
    @ManyToOne(cascade = CascadeType.ALL)
    private EntityUser userIssue;
    @OneToMany
    private List<EntityComment> comments;
    @ManyToOne
    private EntityRepository repository;

    public EntityIssue() {
        comments = new ArrayList<EntityComment>();
        labels = new ArrayList<EntityLabel>();
        mineredAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityUser getAssignee() {
        return assignee;
    }

    public void setAssignee(EntityUser assignee) {
        this.assignee = assignee;
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

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public List<EntityComment> getComments() {
        return comments;
    }

    public void setComments(List<EntityComment> comments) {
        this.comments = comments;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public long getIdIssue() {
        return idIssue;
    }

    public void setIdIssue(long idIssue) {
        this.idIssue = idIssue;
    }

    public List<EntityLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<EntityLabel> labels) {
        this.labels = labels;
    }

    public EntityMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(EntityMilestone milestone) {
        this.milestone = milestone;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public EntityPullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(EntityPullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public String getStateIssue() {
        return stateIssue;
    }

    public void setStateIssue(String stateIssue) {
        this.stateIssue = stateIssue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public EntityUser getUserIssue() {
        return userIssue;
    }

    public void setUserIssue(EntityUser userIssue) {
        this.userIssue = userIssue;
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
        if (!(object instanceof EntityIssue)) {
            return false;
        }
        EntityIssue other = (EntityIssue) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityIssue[ id=" + id + " ]";
    }

    public void addComment(EntityComment comment) {
        if (!getComments().contains(comment) || comment.getId() == null || comment.getId().equals(new Long(0))) {
            getComments().add(comment);
        }
    }
}
