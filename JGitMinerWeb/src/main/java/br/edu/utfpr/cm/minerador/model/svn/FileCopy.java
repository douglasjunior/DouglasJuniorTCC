package br.edu.utfpr.cm.minerador.model.svn;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "file_copies")

public class FileCopy implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "to_id")
    private Integer toId;
    @Column(name = "from_id")
    private Integer fromId;
    @Column(name = "from_commit_id")
    private Integer fromCommitId;
    @Lob
    @Column(name = "new_file_name")
    private String newFileName;
    @Column(name = "action_id")
    private Integer actionId;

    public FileCopy() {
    }

    public FileCopy(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getFromCommitId() {
        return fromCommitId;
    }

    public void setFromCommitId(Integer fromCommitId) {
        this.fromCommitId = fromCommitId;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
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
        if (!(object instanceof FileCopy)) {
            return false;
        }
        FileCopy other = (FileCopy) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.minerador.model.issue.FileCopies[ id=" + id + " ]";
    }
    
}
