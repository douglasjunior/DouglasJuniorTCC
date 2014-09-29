package br.edu.utfpr.cm.minerador.model.issue;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "related_to")

public class RelatedTo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "issue_id")
    private int issueId;
    @Basic(optional = false)
    @Column(name = "related_to")
    private int relatedTo;
    @Basic(optional = false)
    @Column(name = "type")
    private String type;

    public RelatedTo() {
    }

    public RelatedTo(Integer id) {
        this.id = id;
    }

    public RelatedTo(Integer id, int issueId, int relatedTo, String type) {
        this.id = id;
        this.issueId = issueId;
        this.relatedTo = relatedTo;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(int relatedTo) {
        this.relatedTo = relatedTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        if (!(object instanceof RelatedTo)) {
            return false;
        }
        RelatedTo other = (RelatedTo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.minerador.model.issue.RelatedTo[ id=" + id + " ]";
    }

}
