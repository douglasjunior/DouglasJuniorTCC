package br.edu.utfpr.cm.minerador.services.metric.model;

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

    public static final String HEADER
            = "issue;"
            + "issueType;"
            + "issuePriority;"
            + "issueAssignedTo;"
            + "issueSubmittedBy;"
            + "issueWatchers;"
            + "issueReopened;" // quantidade em que foi reaberto (status = reopened)
            + "commenters;"
            + "devCommenters;"
            + "comments;"
            + "wordiness;";

    private final Integer issueNumber;
    private final String issueKey;
    private final String url;
    private final String issueBody;
    private final String issueType;
    private final String priority;
    private final String assignedTo;
    private final String submittedBy;
    private final Integer numberOfWatchers;
    private final Integer reopenedTimes;
    private final List<String> comments;
    private final Integer commenters;
    private final Integer devCommenters;
    private long wordiness;

    public IssueMetrics(Integer issueNumber, String issueKey, String url, String issueBody,
            String issueType, String priority, String assignedTo, String submittedBy,
            Integer numberOfWatchers, Integer reopenedTimes, List<String> comments, Integer commenters, Integer devCommenters) {
        this.issueNumber = issueNumber;
        this.issueKey = issueKey;
        this.url = url;
        this.issueBody = issueBody;
        this.priority = priority;
        this.assignedTo = assignedTo;
        this.submittedBy = submittedBy;
        this.issueType = issueType;
        this.numberOfWatchers = numberOfWatchers;
        this.reopenedTimes = reopenedTimes;

        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = new ArrayList<>();
        }

        this.commenters = commenters;
        this.devCommenters = devCommenters;
    }

    public IssueMetrics(Integer issueNumber, String issueKey, String issueBody, List<String> comments) {
        this.issueNumber = issueNumber;
        this.issueKey = issueKey;
        this.issueBody = issueBody;

        this.url = "";
        this.priority = "";
        this.assignedTo = "";
        this.submittedBy = "";
        this.reopenedTimes = 0;
        this.issueType = "";
        this.numberOfWatchers = 0;
        this.comments = Collections.EMPTY_LIST;

        this.commenters = 0;
        this.devCommenters = 0;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public String getIssueKey() {
        return issueKey;
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

    public Integer getNumberOfWatchers() {
        return numberOfWatchers;
    }

    public Integer getReopenedTimes() {
        return reopenedTimes;
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
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.issueNumber);
        return hash;
    }

    @Override
    public String toString() {
        return issueKey + ";" + issueType + ";"
                + priority + ";" + assignedTo + ";" + submittedBy + ";"
                + numberOfWatchers + ";" + reopenedTimes + ";"
                + commenters + ";" + devCommenters + ";"
                + comments.size() + ";" + wordiness + ";";
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

        return Objects.equals(this.issueNumber, other.issueNumber);
    }

}
