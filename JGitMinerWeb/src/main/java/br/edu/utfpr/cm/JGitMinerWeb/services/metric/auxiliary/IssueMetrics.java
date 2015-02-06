package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.discussion.WordinessCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author geazzy
 */
public class IssueMetrics {

    private final Integer issueNumber;
    private final String url;
    private final String issueBody;
    private final String issueType;
    private final String priority;
    private final String assignedTo;
    private final String submittedBy;
    private final List<String> comments;
    private long wordiness;

    public IssueMetrics(Integer issueNumber, String url, String issueBody, List<String> comments) {
        this.issueNumber = issueNumber;
        this.url = url;
        this.issueBody = issueBody;
        this.priority = "";
        this.assignedTo = "";
        this.submittedBy = "";
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = new ArrayList<>();
        }
        this.issueType = "";
    }

    public IssueMetrics(Integer issueNumber, String url, String issueBody, String issueType, String priority, String assignedTo, String submittedBy, List<String> comments) {
        this.issueNumber = issueNumber;
        this.url = url;
        this.issueBody = issueBody;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.submittedBy = submittedBy;
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = new ArrayList<>();
        }
        this.issueType = issueType;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public String getUrl() {
        return url;
    }

    public String getIssueBody() {
        return issueBody;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getPriority() {
        return priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    public long getWordiness() {
        if (wordiness == 0) {
            wordiness = WordinessCalculator.calcule(this);
        }
        return wordiness;
    }

    public int getNumberOfComments() {
        return comments.size();
    }

    @Override
    public String toString() {
        return issueNumber + ";" + url;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.issueNumber);
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
        final IssueMetrics other = (IssueMetrics) obj;
        if (!Objects.equals(this.issueNumber, other.issueNumber)) {
            return false;
        }
        return true;
    }

}
