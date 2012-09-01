/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import br.edu.utfpr.cm.JGitMinerWeb.services.MilestoneServices;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.eclipse.egit.github.core.Milestone;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitMilestone")
@NamedQueries({
    @NamedQuery(name = "Milestone.findByURL", query = "SELECT m FROM EntityMilestone m WHERE m.url = :url")
})
public class EntityMilestone implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dueOn;
    private int closedIssues;
    private int number;
    private int openIssues;
    @Column(columnDefinition = "text")
    private String description;
    private String stateMilestone;
    @Column(columnDefinition = "text")
    private String title;
    private String url;
    @ManyToOne(cascade = CascadeType.ALL)
    private EntityUser creator;

    public EntityMilestone() {
        mineredAt = new Date();
    }

    public EntityMilestone(Date createdAt, Date dueOn, int closedIssues, int number, int openIssues, String description, String stateMilestone, String title, String url, EntityUser creator) {
        this();
        this.createdAt = createdAt;
        this.dueOn = dueOn;
        this.closedIssues = closedIssues;
        this.number = number;
        this.openIssues = openIssues;
        this.description = description;
        this.stateMilestone = stateMilestone;
        this.title = title;
        this.url = url;
        this.creator = creator;
    }

    public EntityMilestone(Milestone milestone) {
        this();
        this.createdAt = milestone.getCreatedAt();
        this.dueOn = milestone.getDueOn();
        this.closedIssues = milestone.getClosedIssues();
        this.number = milestone.getNumber();
        this.openIssues = milestone.getOpenIssues();
        this.description = milestone.getDescription();
        this.stateMilestone = milestone.getState();
        this.title = milestone.getTitle();
        this.url = milestone.getUrl();
        this.creator = EntityUser.createUser(milestone.getCreator());
    }

    public static EntityMilestone createMilestone(Milestone girMilestone) {
        EntityMilestone entityMilestone = null;
        if (girMilestone != null) {
            entityMilestone = MilestoneServices.getMilestoneByURL(girMilestone.getUrl());
            if (entityMilestone == null) { // se não existe ele cria
                entityMilestone = MilestoneServices.insert(girMilestone);
                System.out.println("############# CRIOU NOVO MILESTONET " + entityMilestone.getUrl() + " #############");
            } else {
                System.out.println("############### PEGOU O MILESTONET " + entityMilestone.getUrl() + " ##############");
            }
        }
        return entityMilestone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getClosedIssues() {
        return closedIssues;
    }

    public void setClosedIssues(int closedIssues) {
        this.closedIssues = closedIssues;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public EntityUser getCreator() {
        return creator;
    }

    public void setCreator(EntityUser creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueOn() {
        return dueOn;
    }

    public void setDueOn(Date dueOn) {
        this.dueOn = dueOn;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(int openIssues) {
        this.openIssues = openIssues;
    }

    public String getStateMilestone() {
        return stateMilestone;
    }

    public void setStateMilestone(String stateMilestone) {
        this.stateMilestone = stateMilestone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        if (!(object instanceof EntityMilestone)) {
            return false;
        }
        EntityMilestone other = (EntityMilestone) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityMilestone[ id=" + id + " ]";
    }
}
