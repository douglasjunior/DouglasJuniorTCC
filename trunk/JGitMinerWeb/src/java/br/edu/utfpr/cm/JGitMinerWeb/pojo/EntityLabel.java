/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import br.edu.utfpr.cm.JGitMinerWeb.services.LabelServices;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.eclipse.egit.github.core.Label;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitLabel")
@NamedQueries({
    @NamedQuery(name = "Label.findByURL", query = "SELECT l FROM EntityLabel l WHERE l.url = :url")
})
public class EntityLabel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    private String color;
    private String name;
    private String url;

    public EntityLabel() {
        mineredAt = new Date();
    }

    public EntityLabel(Label label) {
        this();
        setColor(label.getColor());
        setName(label.getName());
        setUrl(label.getUrl());
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(object instanceof EntityLabel)) {
            return false;
        }
        EntityLabel other = (EntityLabel) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityLabel[ id=" + id + " ]";
    }
}
