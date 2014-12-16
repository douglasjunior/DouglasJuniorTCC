package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOutput {

    private final FilePair filePair;
    private final Set<Integer> issuesId;
    private final Set<Integer> commitsId;
    private final Set<Integer> defectIssuesId;
    private final Set<Integer> futureDefectIssuesId;
    private FilePairApriori filePairApriori;
    private String risky = "NORISKY";

    public FilePairOutput(FilePair filePair) {
        this.filePair = filePair;
        this.issuesId = new HashSet<>();
        this.commitsId = new HashSet<>();
        this.defectIssuesId = new HashSet<>();
        this.futureDefectIssuesId = new HashSet<>();
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public void setFilePairApriori(FilePairApriori filePairApriori) {
        this.filePairApriori = filePairApriori;
    }

    public FilePairApriori getFilePairApriori() {
        return filePairApriori;
    }

    public Set<Integer> getCommitsId() {
        return Collections.unmodifiableSet(commitsId);
    }

    public int getCommitsIdWeight() {
        return commitsId.size();
    }

    public Set<Integer> getIssuesId() {
        return Collections.unmodifiableSet(issuesId);
    }

    public int getIssuesIdWeight() {
        return issuesId.size();
    }

    public Set<Integer> getFutureDefectIssuesId() {
        return Collections.unmodifiableSet(futureDefectIssuesId);
    }

    public int getFutureDefectIssuesIdWeight() {
        return futureDefectIssuesId.size();
    }

    public void addIssueId(Integer issueId) {
        issuesId.add(issueId);
    }

    public void addCommitId(Integer commitId) {
        commitsId.add(commitId);
    }

    public void addDefectIssueId(Integer defectId) {
        defectIssuesId.add(defectId);
    }

    public void addDefectIssuesId(Collection<Integer> defectId) {
        futureDefectIssuesId.addAll(defectId);
    }

    public void addFutureDefectIssuesId(Integer futureDefectId) {
        futureDefectIssuesId.add(futureDefectId);
    }

    public void addFutureDefectIssuesId(Collection<Integer> futureDefectId) {
        futureDefectIssuesId.addAll(futureDefectId);
    }

    public String getRisky() {
        return risky;
    }

    public void changeToRisky() {
        risky = "RISKY";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.filePair);
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
        final FilePairOutput other = (FilePairOutput) obj;
        return Objects.equals(this.filePair, other.filePair);
    }

    /**
     * Prints the filename, filename2. The issueWeight and issues are printed
     * only if was added issues.
     *
     * @return String The attributes separated by semicolon (;)
     */
    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append(filePair.toString(filePairApriori));

        appendInteger(toString, issuesId.size());
        appendSetInteger(toString, issuesId);

        appendInteger(toString, commitsId.size());
        appendSetInteger(toString, commitsId);

        appendInteger(toString, defectIssuesId.size());
        appendSetInteger(toString, defectIssuesId);

        appendInteger(toString, futureDefectIssuesId.size());
        appendSetInteger(toString, futureDefectIssuesId);

        toString.append(filePairApriori.toString());

        return toString.append(risky).append(';').toString();
    }

    public static String getToStringHeader() {
        return FilePair.getToStringHeader()
                + "issues;issuesId;commits;commitsId;"
                + "defectIssues;defectIssuesId;"
                + "futureDefectIssuesId;futureDefectIssues;"
                + FilePairApriori.getToStringHeader()
                + "risk;";
    }

    private void appendInteger(StringBuilder toString, Integer integer) {
        toString.append(integer).append(';');
    }

    private void appendSetInteger(StringBuilder toString, Set<Integer> set) {
        boolean appendComma = false;
        for (Integer integer : set) {
            if (appendComma) {
                toString.append(',');
            }
            toString.append(integer);
            appendComma = true;
        }
        toString.append(';');
    }
}
