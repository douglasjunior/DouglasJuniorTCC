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

    protected final FilePair filePair;
    protected final Set<Integer> issuesId;
    protected final Set<Integer> commitsId;
    protected final Set<Integer> commitsFile1Id;
    protected final Set<Integer> commitsFile2Id;
    protected final Set<Integer> defectIssuesId;
    protected final Set<Integer> futureIssuesId;
    protected final Set<Integer> futureDefectIssuesId;

    public FilePairOutput(FilePair filePair) {
        this.filePair = filePair;
        this.issuesId = new HashSet<>();
        this.commitsId = new HashSet<>();
        this.commitsFile1Id = new HashSet<>();
        this.commitsFile2Id = new HashSet<>();
        this.defectIssuesId = new HashSet<>();
        this.futureIssuesId = new HashSet<>();
        this.futureDefectIssuesId = new HashSet<>();
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public Set<Integer> getCommitsId() {
        return Collections.unmodifiableSet(commitsId);
    }

    public int getCommitsIdWeight() {
        return commitsId.size();
    }

    public Set<Integer> getCommitsFile1Id() {
        return Collections.unmodifiableSet(commitsFile1Id);
    }

    public int getCommitsFile1IdWeight() {
        return commitsFile2Id.size();
    }

    public Set<Integer> getCommitsFile2Id() {
        return Collections.unmodifiableSet(commitsFile2Id);
    }

    public int getCommitsFile2IdWeight() {
        return commitsFile2Id.size();
    }

    public Set<Integer> getIssuesId() {
        return Collections.unmodifiableSet(issuesId);
    }

    public int getIssuesIdWeight() {
        return issuesId.size();
    }

    public Set<Integer> getDefectIssuesId() {
        return defectIssuesId;
    }

    public Set<Integer> getFutureDefectIssuesId() {
        return Collections.unmodifiableSet(futureDefectIssuesId);
    }

    public int getFutureDefectIssuesIdWeight() {
        return futureDefectIssuesId.size();
    }

    public void addIssueId(Integer issueId) {
        this.issuesId.add(issueId);
    }

    public void addCommitId(Integer commitId) {
        this.commitsId.add(commitId);
    }

    public void addCommitFile1Id(Integer commitId) {
        this.commitsFile1Id.add(commitId);
    }

    public void addCommitFile2Id(Integer commitId) {
        this.commitsFile2Id.add(commitId);
    }

    public void addDefectIssueId(Integer defectId) {
        this.defectIssuesId.add(defectId);
    }

    public void addDefectIssuesId(Collection<Integer> defectId) {
        this.futureDefectIssuesId.addAll(defectId);
    }

    public void addFutureIssuesId(Integer futureIssueId) {
        this.futureIssuesId.add(futureIssueId);
    }

    public void addFutureIssuesId(Collection<Integer> futureIssuesId) {
        this.futureIssuesId.addAll(futureIssuesId);
    }

    public void addFutureDefectIssuesId(Integer futureDefectId) {
        this.futureDefectIssuesId.add(futureDefectId);
    }

    public void addFutureDefectIssuesId(Collection<Integer> futureDefectId) {
        this.futureDefectIssuesId.addAll(futureDefectId);
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
        final FilePairAprioriOutput other = (FilePairAprioriOutput) obj;
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

        toString.append(filePair.toString());

        appendInteger(toString, issuesId.size());
        appendSetInteger(toString, issuesId);

        appendInteger(toString, commitsId.size());
        appendSetInteger(toString, commitsId);

        appendInteger(toString, commitsFile1Id.size());
        appendSetInteger(toString, commitsFile1Id);

        appendInteger(toString, commitsFile2Id.size());
        appendSetInteger(toString, commitsFile2Id);

        appendInteger(toString, defectIssuesId.size());
        appendSetInteger(toString, defectIssuesId);

        appendInteger(toString, futureDefectIssuesId.size());
        appendSetInteger(toString, futureDefectIssuesId);

        appendInteger(toString, futureIssuesId.size());
        appendSetInteger(toString, futureIssuesId);

        return toString.toString();
    }

    public String toStringPairFile() {
        return filePair.toString();
    }

    protected void appendInteger(StringBuilder toString, Integer integer) {
        toString.append(integer).append(';');
    }

    protected void appendSetInteger(StringBuilder toString, Set<Integer> set) {
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

    public static String getToStringHeader() {
        return FilePair.getToStringHeader()
                + "issues;issuesId;"
                + "commits;commitsId;"
                + "commitsFile1;commitsFile1Id;"
                + "commitsFile2;commitsFile2Id;"
                + "defectIssues;defectIssuesId;"
                + "futureDefectIssues;futureDefectIssuesId;"
                + "futureIssues;futureIssuesId;";
    }
}
