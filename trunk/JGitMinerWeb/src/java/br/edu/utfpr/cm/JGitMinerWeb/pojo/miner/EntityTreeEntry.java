/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitTreeEntry")
@NamedQueries({
    @NamedQuery(name = "TreeEntry.findByURL", query = "SELECT t FROM EntityTreeEntry t WHERE t.url = :url")
})
public class EntityTreeEntry implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    private long sizeTreeEntry;
    @Column(columnDefinition = "text")
    private String mode;
    @Column(columnDefinition = "text")
    private String pathTreeEntry;
    @Column(columnDefinition = "text")
    private String sha;
    private String type;
    @Column(columnDefinition = "text")
    private String url;
    @ManyToOne
    private EntityTree tree;

    public EntityTreeEntry() {
        mineredAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPathTreeEntry() {
        return pathTreeEntry;
    }

    public void setPathTreeEntry(String pathTreeEntry) {
        this.pathTreeEntry = pathTreeEntry;
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

    public long getSizeTreeEntry() {
        return sizeTreeEntry;
    }

    public void setSizeTreeEntry(long sizeTreeEntry) {
        this.sizeTreeEntry = sizeTreeEntry;
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

    public EntityTree getTree() {
        return tree;
    }

    public void setTree(EntityTree tree) {
        this.tree = tree;
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
        if (!(object instanceof EntityTreeEntry)) {
            return false;
        }
        EntityTreeEntry other = (EntityTreeEntry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityTreeEntry[ id=" + id + " ]";
    }
}
