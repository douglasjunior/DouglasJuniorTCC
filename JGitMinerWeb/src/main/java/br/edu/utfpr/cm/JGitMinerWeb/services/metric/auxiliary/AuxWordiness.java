package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author geazzy
 */
public class AuxWordiness {

    private final Integer issueNumber;
    private final String url;
    private final String issueBody;
    private final List<String> comments;

    public AuxWordiness(Integer issueNumber, String url, String issueBody, List<String> comments) {
        this.issueNumber = issueNumber;
        this.url = url;
        this.issueBody = issueBody;
        this.comments = comments;
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

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    @Override
    public String toString() {
        return issueNumber + ";" + url;
    }

}
