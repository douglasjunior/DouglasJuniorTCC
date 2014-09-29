package br.edu.utfpr.cm.minerador.model.svn;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "scmlog")

public class Scmlog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    private Integer id;
    @Lob
    @Column(name = "rev")
    private String rev;
    @Column(name = "committer_id")
    private Integer committerId;
    @Column(name = "author_id")
    private Integer authorId;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "date_tz")
    private Integer dateTz;
    @Column(name = "author_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date authorDate;
    @Column(name = "author_date_tz")
    private Integer authorDateTz;
    @Lob
    @Column(name = "message")
    private String message;
    @Column(name = "composed_rev")
    private Boolean composedRev;
    @Column(name = "repository_id")
    private Integer repositoryId;

    public Scmlog() {
    }

    public Scmlog(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public Integer getCommitterId() {
        return committerId;
    }

    public void setCommitterId(Integer committerId) {
        this.committerId = committerId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getDateTz() {
        return dateTz;
    }

    public void setDateTz(Integer dateTz) {
        this.dateTz = dateTz;
    }

    public Date getAuthorDate() {
        return authorDate;
    }

    public void setAuthorDate(Date authorDate) {
        this.authorDate = authorDate;
    }

    public Integer getAuthorDateTz() {
        return authorDateTz;
    }

    public void setAuthorDateTz(Integer authorDateTz) {
        this.authorDateTz = authorDateTz;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getComposedRev() {
        return composedRev;
    }

    public void setComposedRev(Boolean composedRev) {
        this.composedRev = composedRev;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
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
        if (!(object instanceof Scmlog)) {
            return false;
        }
        Scmlog other = (Scmlog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "commit: " + id + " message: " + message;
    }

}
