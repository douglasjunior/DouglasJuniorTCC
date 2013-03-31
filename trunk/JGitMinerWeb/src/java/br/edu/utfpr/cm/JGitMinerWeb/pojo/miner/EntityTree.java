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
@Table(name = "gitTree")
@NamedQueries({
    @NamedQuery(name = "Tree.findByURL", query = "SELECT t FROM EntityTree t WHERE t.url = :url")
})
public class EntityTree implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @OneToMany(mappedBy = "tree")
    private List<EntityTreeEntry> treeEntrys;
    @Column(columnDefinition = "text")
    private String sha;
    @Column(columnDefinition = "text")
    private String url;

    public EntityTree() {
        treeEntrys = new ArrayList<EntityTreeEntry>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Date getMineredAt() {
        return mineredAt;
    }

    public void setMineredAt(Date mineredAt) {
        this.mineredAt = mineredAt;
    }

    public List<EntityTreeEntry> getTreeEntrys() {
        return treeEntrys;
    }

    public void setTreeEntrys(List<EntityTreeEntry> treeEntrys) {
        this.treeEntrys = treeEntrys;
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
        if (!(object instanceof EntityTree)) {
            return false;
        }
        EntityTree other = (EntityTree) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityTree[ id=" + id + " ]";
    }

    public void addTreeEntry(EntityTreeEntry treeEntry) {
        if (!treeEntrys.contains(treeEntry)) {
            treeEntrys.add(treeEntry);
        }
        treeEntry.setTree(this);
    }
}
