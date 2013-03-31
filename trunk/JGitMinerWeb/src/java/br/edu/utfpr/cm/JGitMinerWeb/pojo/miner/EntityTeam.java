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
@Table(name = "gitTeam")
@NamedQueries({
    @NamedQuery(name = "Team.findByTeamID", query = "SELECT t FROM EntityTeam t WHERE t.idTeam = :idTeam")
})
public class EntityTeam implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineratedAt;
    @Column(unique = true)
    private Integer idTeam;
    private Integer membersCount;
    private Integer reposCount;
    private String name;
    private String permission;
    private String url;
    @OneToMany
    private List<EntityUser> members;
    @ManyToMany
    private List<EntityRepository> repositories;

    public EntityTeam() {
        members = new ArrayList<EntityUser>();
        repositories = new ArrayList<EntityRepository>();
        mineratedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdTeam() {
        return idTeam;
    }

    public void setIdTeam(Integer idTeam) {
        this.idTeam = idTeam;
    }

    public List<EntityUser> getMembers() {
        return members;
    }

    public void setMembers(List<EntityUser> members) {
        this.members = members;
    }

    public List<EntityRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<EntityRepository> repositories) {
        this.repositories = repositories;
    }

    public Integer getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }

    public Date getMineratedAt() {
        return mineratedAt;
    }

    public void setMineratedAt(Date mineratedAt) {
        this.mineratedAt = mineratedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Integer getReposCount() {
        return reposCount;
    }

    public void setReposCount(Integer reposCount) {
        this.reposCount = reposCount;
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
        if (!(object instanceof EntityTeam)) {
            return false;
        }
        EntityTeam other = (EntityTeam) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityTeam[ id=" + id + " ]";
    }
}
