/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import br.edu.utfpr.cm.JGitMinerWeb.services.PullRequestMakerServices;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.eclipse.egit.github.core.PullRequestMarker;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitPullRequestMaker")
public class EntityPullRequestMarker implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @ManyToOne
    private EntityRepository repo;
    @Column(columnDefinition = "text")
    private String label;
    @Column(columnDefinition = "text")
    private String refPullRequestMarker;
    @Column(columnDefinition = "text")
    private String sha;
    @ManyToOne
    private EntityUser user;

    public EntityPullRequestMarker() {
        mineredAt = new Date();
    }

    public EntityPullRequestMarker(PullRequestMarker gitMaker) {
        this();
//        this.repo = EntityRepository.create(gitMaker.getRepo());
        this.label = gitMaker.getLabel();
        this.refPullRequestMarker = gitMaker.getRef();
        this.sha = gitMaker.getSha();
//        this.user = EntityUser.createUser(gitMaker.getUser());
    }

    public static EntityPullRequestMarker create(PullRequestMarker gitMaker) {
        EntityPullRequestMarker entityMaker = null;
        if (gitMaker != null) {
            entityMaker = PullRequestMakerServices.insert(gitMaker);
            System.out.println("############# CRIOU NOVO PULL REQUEST MAKER " + entityMaker.getId() + " #############");
        }
        return entityMaker;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRefPullRequestMarker() {
        return refPullRequestMarker;
    }

    public void setRefPullRequestMarker(String refPullRequestMarker) {
        this.refPullRequestMarker = refPullRequestMarker;
    }

    public EntityRepository getRepo() {
        return repo;
    }

    public void setRepo(EntityRepository repo) {
        this.repo = repo;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public EntityUser getUser() {
        return user;
    }

    public void setUser(EntityUser user) {
        this.user = user;
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
        if (!(object instanceof EntityPullRequestMarker)) {
            return false;
        }
        EntityPullRequestMarker other = (EntityPullRequestMarker) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityPullRequestMarker[ id=" + id + " ]";
    }
}
