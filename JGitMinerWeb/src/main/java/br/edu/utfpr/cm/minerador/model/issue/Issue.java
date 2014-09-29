package br.edu.utfpr.cm.minerador.model.issue;

import br.edu.utfpr.cm.minerador.model.svn.Scmlog;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "issues")

public class Issue implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "tracker_id")
    private int trackerId;
    @Basic(optional = false)
    @Column(name = "issue")
    private String issue;
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @Column(name = "summary")
    private String summary;
    @Basic(optional = false)
    @Lob
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Column(name = "resolution")
    private String resolution;
    @Column(name = "priority")
    private String priority;
    @Basic(optional = false)
    @Column(name = "submitted_by")
    private int submittedBy;
    @Basic(optional = false)
    @Column(name = "submitted_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedOn;
    @Basic(optional = false)
    @Column(name = "assigned_to")
    private int assignedTo;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "issue", orphanRemoval = true)
    private Set<IssueScmlog> scmlogs;

    public Issue() {
        scmlogs = new HashSet<>();
    }

    public Issue(Integer id) {
        this();
        this.id = id;
    }

    public Issue(Integer id, int trackerId, String issue, String summary, String description, String status, int submittedBy, Date submittedOn, int assignedTo) {
        this();
        this.id = id;
        this.trackerId = trackerId;
        this.issue = issue;
        this.summary = summary;
        this.description = description;
        this.status = status;
        this.submittedBy = submittedBy;
        this.submittedOn = submittedOn;
        this.assignedTo = assignedTo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(int trackerId) {
        this.trackerId = trackerId;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(int submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Date getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(Date submittedOn) {
        this.submittedOn = submittedOn;
    }

    public int getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Set<IssueScmlog> getScmlogs() {
        return scmlogs;
    }

    public void setScmlogs(Set<IssueScmlog> scmlogs) {
        this.scmlogs = scmlogs;
    }

    public void addScmlog(Scmlog scmlog) {
        scmlogs.add(new IssueScmlog(scmlog, this));
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
        if (!(object instanceof Issue)) {
            return false;
        }
        Issue other = (Issue) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.minerador.model.issue.Issues[ id=" + id + " ]";
    }

}
