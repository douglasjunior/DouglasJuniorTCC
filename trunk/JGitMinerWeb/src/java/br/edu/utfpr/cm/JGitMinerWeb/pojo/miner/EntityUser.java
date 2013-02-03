/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

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
@Table(name = "gitUser")
@NamedQueries({
    @NamedQuery(name = "User.findByLogin", query = "SELECT u FROM EntityUser u WHERE u.login = :login")
})
public class EntityUser implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean hireable;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    private int collaborators;
    private int diskUsage;
    private int followers;
    private int following;
    private int idUser;
    private int ownedPrivateRepos;
    private int privateGists;
    private int publicGists;
    private int publicRepos;
    private int totalPrivateRepos;
    @Column(columnDefinition = "text")
    private String avatarUrl;
    @Column(columnDefinition = "text")
    private String blog;
    @Column(columnDefinition = "text")
    private String company;
    private String email;
    @Column(columnDefinition = "text")
    private String gravatarId;
    @Column(columnDefinition = "text")
    private String htmlUrl;
    private String location;
    @Column(unique = true)
    private String login;
    private String name;
    @Column(columnDefinition = "text")
    private String type;
    @Column(columnDefinition = "text")
    private String url;
    @OneToMany(mappedBy = "userIssue")
    private List<EntityIssue> issues;
    @OneToMany(mappedBy = "assignee")
    private List<EntityIssue> issuesAssigned;
    @JoinTable(name = "gitRepository_userWatchers")
    @ManyToMany
    private List<EntityRepository> watchedRepositories;
    @JoinTable(name = "gitRepository_userCollaborators")
    @ManyToMany
    private List<EntityRepository> collaboratedRepositories;

    public EntityUser() {
        issues = new ArrayList<EntityIssue>();
        issuesAssigned = new ArrayList<EntityIssue>();
        collaboratedRepositories = new ArrayList<EntityRepository>();
        watchedRepositories = new ArrayList<EntityRepository>();
        mineredAt = new Date();
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public int getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(int collaborators) {
        this.collaborators = collaborators;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(int diskUsage) {
        this.diskUsage = diskUsage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getGravatarId() {
        return gravatarId;
    }

    public void setGravatarId(String gravatarId) {
        this.gravatarId = gravatarId;
    }

    public boolean isHireable() {
        return hireable;
    }

    public void setHireable(boolean hireable) {
        this.hireable = hireable;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idGit) {
        this.idUser = idGit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnedPrivateRepos() {
        return ownedPrivateRepos;
    }

    public void setOwnedPrivateRepos(int ownedPrivateRepos) {
        this.ownedPrivateRepos = ownedPrivateRepos;
    }

    public int getPrivateGists() {
        return privateGists;
    }

    public void setPrivateGists(int privateGists) {
        this.privateGists = privateGists;
    }

    public int getPublicGists() {
        return publicGists;
    }

    public void setPublicGists(int publicGists) {
        this.publicGists = publicGists;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public int getTotalPrivateRepos() {
        return totalPrivateRepos;
    }

    public void setTotalPrivateRepos(int totalPrivateRepos) {
        this.totalPrivateRepos = totalPrivateRepos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<EntityIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<EntityIssue> issues) {
        this.issues = issues;
    }

    public List<EntityIssue> getIssuesAssigned() {
        return issuesAssigned;
    }

    public void setIssuesAssigned(List<EntityIssue> issuesAssigned) {
        this.issuesAssigned = issuesAssigned;
    }

    public void addCollaboratedRepository(EntityRepository repo) {
        if (!collaboratedRepositories.contains(repo)) {
            collaboratedRepositories.add(repo);
        }
        if (!repo.getCollaborators().contains(this)) {
            repo.getCollaborators().add(this);
        }
    }

    public void addWatchedRepository(EntityRepository repo) {
        if (!watchedRepositories.contains(repo)) {
            watchedRepositories.add(repo);
        }
        if (!repo.getWatchers().contains(this)) {
            repo.getWatchers().add(this);
        }
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
        if (!(object instanceof EntityUser)) {
            return false;
        }
        EntityUser other = (EntityUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityUser[ id=" + id + ", login=" + login + "]";
    }
}
