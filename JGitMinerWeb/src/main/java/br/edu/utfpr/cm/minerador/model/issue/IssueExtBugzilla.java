package br.edu.utfpr.cm.minerador.model.issue;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "issues_ext_bugzilla")

public class IssueExtBugzilla implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "alias")
    private String alias;
    @Basic(optional = false)
    @Column(name = "delta_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deltaTs;
    @Column(name = "reporter_accessible")
    private String reporterAccessible;
    @Column(name = "cclist_accessible")
    private String cclistAccessible;
    @Column(name = "classification_id")
    private String classificationId;
    @Column(name = "classification")
    private String classification;
    @Column(name = "product")
    private String product;
    @Column(name = "component")
    private String component;
    @Column(name = "version")
    private String version;
    @Column(name = "rep_platform")
    private String repPlatform;
    @Column(name = "op_sys")
    private String opSys;
    @Column(name = "dup_id")
    private Integer dupId;
    @Lob
    @Column(name = "bug_file_loc")
    private String bugFileLoc;
    @Lob
    @Column(name = "status_whiteboard")
    private String statusWhiteboard;
    @Column(name = "target_milestone")
    private String targetMilestone;
    @Column(name = "votes")
    private Integer votes;
    @Column(name = "everconfirmed")
    private String everconfirmed;
    @Column(name = "qa_contact")
    private String qaContact;
    @Column(name = "estimated_time")
    private String estimatedTime;
    @Column(name = "remaining_time")
    private String remainingTime;
    @Column(name = "actual_time")
    private String actualTime;
    @Column(name = "deadline")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadline;
    @Column(name = "keywords")
    private String keywords;
    @Column(name = "flag")
    private String flag;
    @Column(name = "cc")
    private String cc;
    @Column(name = "group_bugzilla")
    private String groupBugzilla;
    @Basic(optional = false)
    @Column(name = "issue_id")
    private int issueId;

    public IssueExtBugzilla() {
    }

    public IssueExtBugzilla(Integer id) {
        this.id = id;
    }

    public IssueExtBugzilla(Integer id, Date deltaTs, int issueId) {
        this.id = id;
        this.deltaTs = deltaTs;
        this.issueId = issueId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getDeltaTs() {
        return deltaTs;
    }

    public void setDeltaTs(Date deltaTs) {
        this.deltaTs = deltaTs;
    }

    public String getReporterAccessible() {
        return reporterAccessible;
    }

    public void setReporterAccessible(String reporterAccessible) {
        this.reporterAccessible = reporterAccessible;
    }

    public String getCclistAccessible() {
        return cclistAccessible;
    }

    public void setCclistAccessible(String cclistAccessible) {
        this.cclistAccessible = cclistAccessible;
    }

    public String getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(String classificationId) {
        this.classificationId = classificationId;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRepPlatform() {
        return repPlatform;
    }

    public void setRepPlatform(String repPlatform) {
        this.repPlatform = repPlatform;
    }

    public String getOpSys() {
        return opSys;
    }

    public void setOpSys(String opSys) {
        this.opSys = opSys;
    }

    public Integer getDupId() {
        return dupId;
    }

    public void setDupId(Integer dupId) {
        this.dupId = dupId;
    }

    public String getBugFileLoc() {
        return bugFileLoc;
    }

    public void setBugFileLoc(String bugFileLoc) {
        this.bugFileLoc = bugFileLoc;
    }

    public String getStatusWhiteboard() {
        return statusWhiteboard;
    }

    public void setStatusWhiteboard(String statusWhiteboard) {
        this.statusWhiteboard = statusWhiteboard;
    }

    public String getTargetMilestone() {
        return targetMilestone;
    }

    public void setTargetMilestone(String targetMilestone) {
        this.targetMilestone = targetMilestone;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getEverconfirmed() {
        return everconfirmed;
    }

    public void setEverconfirmed(String everconfirmed) {
        this.everconfirmed = everconfirmed;
    }

    public String getQaContact() {
        return qaContact;
    }

    public void setQaContact(String qaContact) {
        this.qaContact = qaContact;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getActualTime() {
        return actualTime;
    }

    public void setActualTime(String actualTime) {
        this.actualTime = actualTime;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getGroupBugzilla() {
        return groupBugzilla;
    }

    public void setGroupBugzilla(String groupBugzilla) {
        this.groupBugzilla = groupBugzilla;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
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
        if (!(object instanceof IssueExtBugzilla)) {
            return false;
        }
        IssueExtBugzilla other = (IssueExtBugzilla) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.minerador.model.issue.IssuesExtBugzilla[ id=" + id + " ]";
    }

}
