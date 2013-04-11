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
@Table(name = "gitRepository")
@NamedQueries({
    @NamedQuery(name = "Repository.findByName",
    query = "SELECT r FROM EntityRepository r WHERE r.name = :name"),
    @NamedQuery(name = "Repository.findByNameAndOwner",
    query = "SELECT r FROM EntityRepository r WHERE r.name = :name AND r.owner.login = :login"),
    @NamedQuery(name = "Repository.findByIdRepository",
    query = "SELECT r FROM EntityRepository r WHERE r.idRepository = :idRepository"),
    @NamedQuery(name = "Repository.findByPrimaryMiner",
    query = "SELECT r FROM EntityRepository r WHERE r.primaryMiner = TRUE")
})
public class EntityRepository implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    private boolean primaryMiner;
    private boolean fork;
    private boolean hasDownloads;
    private boolean hasIssues;
    private boolean hasWiki;
    private boolean isPrivate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date pushedAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(unique = true)
    private Long idRepository;
    private Integer sizeRepository;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityRepository parent;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityRepository source;
    private String cloneUrl;
    @Column(columnDefinition = "text")
    private String description;
    @Column(columnDefinition = "text")
    private String homepage;
    private String gitUrl;
    private String htmlUrl;
    @Column(columnDefinition = "text")
    private String languageRepository;
    @Column(columnDefinition = "text")
    private String masterBranch;
    private String mirrorUrl;
    private String name;
    @Column(columnDefinition = "text")
    private String sshUrl;
    @Column(columnDefinition = "text")
    private String svnUrl;
    private String url;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityUser owner;
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private Set<EntityIssue> issues;
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private Set<EntityPullRequest> pullRequests;
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private Set<EntityRepositoryCommit> repoCommits;
    @ManyToMany(mappedBy = "collaboratedRepositories", fetch = FetchType.LAZY)
    private Set<EntityUser> collaborators;
    @ManyToMany(mappedBy = "watchedRepositories", fetch = FetchType.LAZY)
    private Set<EntityUser> watchers;
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<EntityRepository> forks;
    @ManyToMany(mappedBy = "repositories", fetch = FetchType.LAZY)
    private Set<EntityTeam> teams;
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private Set<EntityMilestone> milestones;

    public EntityRepository() {
        issues = new HashSet<>();
        pullRequests = new HashSet<>();
        repoCommits = new HashSet<>();
        collaborators = new HashSet<>();
        watchers = new HashSet<>();
        forks = new HashSet<>();
        teams = new HashSet<>();
        milestones = new HashSet<>();
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

    public String getCloneUrl() {
        return cloneUrl;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public boolean isHasDownloads() {
        return hasDownloads;
    }

    public void setHasDownloads(boolean hasDownloads) {
        this.hasDownloads = hasDownloads;
    }

    public boolean isHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public boolean isHasWiki() {
        return hasWiki;
    }

    public void setHasWiki(boolean hasWiki) {
        this.hasWiki = hasWiki;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Long getIdRepository() {
        return idRepository;
    }

    public void setIdRepository(Long idRepository) {
        this.idRepository = idRepository;
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getLanguageRepository() {
        return languageRepository;
    }

    public void setLanguageRepository(String languageRepository) {
        this.languageRepository = languageRepository;
    }

    public String getMasterBranch() {
        return masterBranch;
    }

    public void setMasterBranch(String masterBranch) {
        this.masterBranch = masterBranch;
    }

    public String getMirrorUrl() {
        return mirrorUrl;
    }

    public void setMirrorUrl(String mirrorUrl) {
        this.mirrorUrl = mirrorUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityUser getOwner() {
        return owner;
    }

    public void setOwner(EntityUser owner) {
        this.owner = owner;
    }

    public Set<EntityIssue> getIssues() {
        return issues;
    }

    public void setIssues(Set<EntityIssue> issues) {
        this.issues = issues;
    }

    public Set<EntityPullRequest> getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(Set<EntityPullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }

    public Set<EntityRepositoryCommit> getRepoCommits() {
        return repoCommits;
    }

    public void setRepoCommits(Set<EntityRepositoryCommit> repoCommits) {
        this.repoCommits = repoCommits;
    }

    public Set<EntityUser> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(Set<EntityUser> collaborators) {
        this.collaborators = collaborators;
    }

    public Set<EntityUser> getWatchers() {
        return watchers;
    }

    public void setWatchers(Set<EntityUser> watchers) {
        this.watchers = watchers;
    }

    public Set<EntityRepository> getForks() {
        return forks;
    }

    public void setForks(Set<EntityRepository> forks) {
        this.forks = forks;
    }

    public Set<EntityTeam> getTeams() {
        return teams;
    }

    public void setTeams(Set<EntityTeam> teams) {
        this.teams = teams;
    }

    public Set<EntityMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(Set<EntityMilestone> milestones) {
        this.milestones = milestones;
    }

    public EntityRepository getParent() {
        return parent;
    }

    public void setParent(EntityRepository parent) {
        this.parent = parent;
    }

    public Date getPushedAt() {
        return pushedAt;
    }

    public void setPushedAt(Date pushedAt) {
        this.pushedAt = pushedAt;
    }

    public Integer getSizeRepository() {
        return sizeRepository;
    }

    public void setSizeRepository(Integer sizeRepository) {
        this.sizeRepository = sizeRepository;
    }

    public EntityRepository getSource() {
        return source;
    }

    public void setSource(EntityRepository source) {
        this.source = source;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    public String getSvnUrl() {
        return svnUrl;
    }

    public void setSvnUrl(String svnUrl) {
        this.svnUrl = svnUrl;
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

    public void addCollaborator(EntityUser collaborator) {
        collaborators.add(collaborator);
    }

    public void addWatcher(EntityUser watcher) {
        if (!watchers.contains(watcher)) {
            watchers.add(watcher);
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
        if (!(object instanceof EntityRepository)) {
            return false;
        }
        EntityRepository other = (EntityRepository) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return owner + "/" + name;
    }

    public void addIssue(EntityIssue issue) {
        if (!getIssues().contains(issue)) {
            getIssues().add(issue);
        }
        issue.setRepository(this);
    }

    public void addFork(EntityRepository fork) {
        if (!forks.contains(fork)) {
            forks.add(fork);
        }
        fork.setParent(this);
    }

    public boolean isPrimaryMiner() {
        return primaryMiner;
    }

    public void setPrimaryMiner(boolean primaryMiner) {
        this.primaryMiner = primaryMiner || this.primaryMiner;
    }

    public void addPullRequest(EntityPullRequest pullRequest) {
        pullRequests.add(pullRequest);
        pullRequest.setRepository(this);
    }

    public void addRepoCommit(EntityRepositoryCommit repoCommit) {
        if (!repoCommits.contains(repoCommit)) {
            repoCommits.add(repoCommit);
        }
        repoCommit.setRepository(this);
    }

    public void addTeam(EntityTeam team) {
        if (!teams.contains(team)) {
            teams.add(team);
        }
        if (!team.getRepositories().contains(this)) {
            team.getRepositories().add(this);
        }
    }

    public void addMilestone(EntityMilestone milestone) {
        if (!milestones.contains(milestone)) {
            milestones.add(milestone);
        }
        milestone.setRepository(this);
    }
}
