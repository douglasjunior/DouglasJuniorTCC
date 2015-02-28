package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Collections;

/**
 *
 * @author geazzy
 */
public final class EmptyIssueMetrics extends IssueMetrics {

    @SuppressWarnings("unchecked")
    public EmptyIssueMetrics() {
        super(0, "", "", "", "", "", "", "", 0, 0, Collections.EMPTY_LIST, 0, 0, null, null);
    }

    @Override
    public String toString() {
        return "";
    }

}
