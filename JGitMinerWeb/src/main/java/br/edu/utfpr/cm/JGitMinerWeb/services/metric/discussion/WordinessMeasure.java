
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.discussion;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class WordinessMeasure {

    private final Integer issueNumber;
    private final long wordiness;

    public WordinessMeasure(Integer issueNumber, long wordiness) {
        this.issueNumber = issueNumber;
        this.wordiness = wordiness;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public long getWordiness() {
        return wordiness;
    }

}
