/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
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
public class EntityUser implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean hireable;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    private Integer collaborators;
    private Integer diskUsage;
    private Integer followers;
    private Integer following;
    private Integer idUser;
    private Integer ownedPrivateRepos;
    private Integer privateGists;
    private Integer publicGists;
    private Integer publicRepos;
    private Integer totalPrivateRepos;
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
    @OneToMany(mappedBy = "userIssue", fetch = FetchType.LAZY)
    private List<EntityIssue> issues;
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<EntityIssue> issuesAssigned;
    @JoinTable(name = "gitRepository_userWatchers")
    @ManyToMany
    private List<EntityRepository> watchedRepositories;
    @JoinTable(name = "gitRepository_userCollaborators")
    @ManyToMany
    private List<EntityRepository> collaboratedRepositories;

    public EntityUser() {
        issues = new ArrayList<>();
        issuesAssigned = new ArrayList<>();
        collaboratedRepositories = new ArrayList<>();
        watchedRepositories = new ArrayList<>();
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

    public Integer getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(Integer collaborators) {
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

    public Integer getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Integer diskUsage) {
        this.diskUsage = diskUsage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getFollowers() {
        return followers;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public Integer getFollowing() {
        return following;
    }

    public void setFollowing(Integer following) {
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

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idGit) {
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

    public Integer getOwnedPrivateRepos() {
        return ownedPrivateRepos;
    }

    public void setOwnedPrivateRepos(Integer ownedPrivateRepos) {
        this.ownedPrivateRepos = ownedPrivateRepos;
    }

    public Integer getPrivateGists() {
        return privateGists;
    }

    public void setPrivateGists(Integer privateGists) {
        this.privateGists = privateGists;
    }

    public Integer getPublicGists() {
        return publicGists;
    }

    public void setPublicGists(Integer publicGists) {
        this.publicGists = publicGists;
    }

    public Integer getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(Integer publicRepos) {
        this.publicRepos = publicRepos;
    }

    public Integer getTotalPrivateRepos() {
        return totalPrivateRepos;
    }

    public void setTotalPrivateRepos(Integer totalPrivateRepos) {
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
        repo.getCollaborators().add(this);
    }

    public void addWatchedRepository(EntityRepository repo) {
        if (!watchedRepositories.contains(repo)) {
            watchedRepositories.add(repo);
        }
        repo.getWatchers().add(this);
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
        return login;
    }
}
