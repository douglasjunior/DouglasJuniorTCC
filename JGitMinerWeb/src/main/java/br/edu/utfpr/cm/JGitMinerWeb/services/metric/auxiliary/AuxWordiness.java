package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = new ArrayList<>();
        }
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
        final AuxWordiness other = (AuxWordiness) obj;
        if (!Objects.equals(this.issueNumber, other.issueNumber)) {
            return false;
        }
        return true;
    }

}
