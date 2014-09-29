package br.edu.utfpr.cm.minerador.model.issue;

import br.edu.utfpr.cm.minerador.model.svn.Scmlog;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "issues_scmlog",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"scmlog_id", "issue_id"}, name = "unq_issue_scmlog")
        },
        indexes = {
            @Index(columnList = "scmlog_id,issue_id", unique = true, name = "unq_issue_scmlog")
        }
)

public class IssueScmlog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "scmlog_id")
    private Integer scmlogId;

    @ManyToOne
    @JoinColumn(name = "issue_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Issue issue;

    public IssueScmlog() {
    }

    public IssueScmlog(Scmlog scmlog, Issue issue) {
        setIssue(issue);
        setScmlogId(scmlog.getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScmlogId() {
        return scmlogId;
    }

    public void setScmlogId(Integer scmlogId) {
        this.scmlogId = scmlogId;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.scmlogId);
        hash = 13 * hash + Objects.hashCode(this.issue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IssueScmlog other = (IssueScmlog) obj;
        if (!Objects.equals(this.scmlogId, other.scmlogId)) {
            return false;
        }
        if (!Objects.equals(this.issue, other.issue)) {
            return false;
        }
        return true;
    }

}
