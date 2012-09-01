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
import org.eclipse.egit.github.core.Repository;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitRepository")
@NamedQueries({
    @NamedQuery(name = "Repository.findByName", query = "SELECT r FROM EntityRepository r WHERE r.name = :name")
})
public class EntityRepository implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Column(length = 100)
    private String ownerLogin;
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
    private int forks;
    @Column(unique = true)
    private long idRepository;
    private int openIssues;
    private int sizeRepository;
    private int watchers;
    @ManyToOne
    private EntityRepository parent;
    @ManyToOne
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
    @ManyToOne
    private EntityUser owner;
    @OneToMany(mappedBy = "repository")
    private List<EntityIssue> issues;

    public EntityRepository() {
        issues = new ArrayList<EntityIssue>();
        mineredAt = new Date();
    }

    public EntityRepository(boolean fork, boolean hasDownloads, boolean hasIssues, boolean hasWiki, boolean isPrivate, Date createdAt, Date pushedAt, Date updatedAt, int forks, long idRepository, int openIssues, int sizeRepository, int watchers, EntityRepository parent, EntityRepository source, String cloneUrl, String description, String homepage, String gitUrl, String htmlUrl, String languageRepository, String masterBranch, String mirrorUrl, String name, String sshUrl, String svnUrl, String url, EntityUser owner) {
        this();
        this.fork = fork;
        this.hasDownloads = hasDownloads;
        this.hasIssues = hasIssues;
        this.hasWiki = hasWiki;
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
        this.pushedAt = pushedAt;
        this.updatedAt = updatedAt;
        this.forks = forks;
        this.idRepository = idRepository;
        this.openIssues = openIssues;
        this.sizeRepository = sizeRepository;
        this.watchers = watchers;
        this.parent = parent;
        this.source = source;
        this.cloneUrl = cloneUrl;
        this.description = description;
        this.homepage = homepage;
        this.gitUrl = gitUrl;
        this.htmlUrl = htmlUrl;
        this.languageRepository = languageRepository;
        this.masterBranch = masterBranch;
        this.mirrorUrl = mirrorUrl;
        this.name = name;
        this.sshUrl = sshUrl;
        this.svnUrl = svnUrl;
        this.url = url;
        this.owner = owner;
    }

    public EntityRepository(Repository repository) {
        this();
        this.fork = repository.isFork();
        this.hasDownloads = repository.isHasDownloads();
        this.hasIssues = repository.isHasIssues();
        this.hasWiki = repository.isHasWiki();
        this.isPrivate = repository.isPrivate();
        this.createdAt = repository.getCreatedAt();
        this.pushedAt = repository.getPushedAt();
        this.updatedAt = repository.getUpdatedAt();
        this.forks = repository.getForks();
        this.idRepository = repository.getId();
        this.openIssues = repository.getOpenIssues();
        this.sizeRepository = repository.getSize();
        this.watchers = repository.getWatchers();
//        this.parent = EntityRepository.create(repository.getParent());
//        this.source = EntityRepository.create(repository.getSource());
        this.cloneUrl = repository.getCloneUrl();
        this.description = repository.getDescription();
        this.homepage = repository.getHomepage();
        this.gitUrl = repository.getGitUrl();
        this.htmlUrl = repository.getHtmlUrl();
        this.languageRepository = repository.getLanguage();
        this.masterBranch = repository.getMasterBranch();
        this.mirrorUrl = repository.getMirrorUrl();
        this.name = repository.getName();
        this.sshUrl = repository.getSshUrl();
        this.svnUrl = repository.getSvnUrl();
        this.url = repository.getUrl();
        this.ownerLogin = repository.getOwner().getLogin();
//        this.owner = EntityUser.createUser(repository.getOwner());
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

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
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

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
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

    public long getIdRepository() {
        return idRepository;
    }

    public void setIdRepository(long idRepository) {
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

    public int getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(int openIssues) {
        this.openIssues = openIssues;
    }

    public EntityUser getOwner() {
        return owner;
    }

    public void setOwner(EntityUser owner) {
        this.owner = owner;
    }

    public List<EntityIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<EntityIssue> issues) {
        this.issues = issues;
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

    public int getSizeRepository() {
        return sizeRepository;
    }

    public void setSizeRepository(int sizeRepository) {
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

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
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
        return name + " [ id=" + id + " ]";
    }

    public void addIssue(EntityIssue issue) {
        if (!getIssues().contains(issue) || issue.getId() == null || issue.getId().equals(new Long(0))) {
            getIssues().add(issue);
        }
        issue.setRepository(this);
    }
}
