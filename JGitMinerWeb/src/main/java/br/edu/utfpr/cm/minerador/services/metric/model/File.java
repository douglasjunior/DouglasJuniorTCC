package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo Kuroda
 */
public class File {

    protected final String fileName;
    private final Set<Integer> issuesId;
    private final Set<Integer> commitsId;
    private final Set<Integer> defectIssuesId;
    private int issuesWeight = 0;
    private int commitsWeight = 0;
    private int defectsWeight = 0;
    private long futureDefects = 0;
    private double support = 0;

    public File(String fileName) {
        this.fileName = fileName;
        this.issuesId = new HashSet<>();
        this.commitsId = new HashSet<>();
        this.defectIssuesId = new HashSet<>();
    }

    public String getFileName() {
        return fileName;
    }

    public Set<Integer> getIssuesId() {
        return Collections.unmodifiableSet(issuesId);
    }

    public Set<Integer> getCommitsId() {
        return Collections.unmodifiableSet(commitsId);
    }

    public Set<Integer> getDefectIssuesId() {
        return Collections.unmodifiableSet(defectIssuesId);
    }

    public void addIssueId(Integer issueId) {
        issuesWeight++;
        issuesId.add(issueId);
    }

    public void addDefectIssueId(Integer issueId) {
        defectsWeight++;
        defectIssuesId.add(issueId);
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public void addCommitId(Integer issueId) {
        commitsWeight++;
        commitsId.add(issueId);
    }

    public int getIssuesWeight() {
        return issuesWeight;
    }

    public int getCommitsWeight() {
        return commitsWeight;
    }

    public int getDefectsWeight() {
        return defectsWeight;
    }

    public void setFutureDefects(long futureDefects) {
        this.futureDefects = futureDefects;
    }

    public long getFutureDefects() {
        return futureDefects;
    }

    /**
     * Compare the filenames. For example, A and B. The pair A + B is equals to
     * B + A.
     *
     * @param obj The object to check equality with this instance
     * @return boolean If the filenames are equals.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof File) {
            File other = (File) obj;
            if (Util.stringEquals(this.fileName, other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash +
                (Objects.hashCode(this.fileName));
        return hash;
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
        toString.append(fileName).append(";");

        appendInteger(toString, issuesWeight);
        appendSetInteger(toString, issuesId);

        appendInteger(toString, commitsWeight);
        appendSetInteger(toString, commitsId);

        if (defectsWeight > 0) {
            appendInteger(toString, defectsWeight);
            appendSetInteger(toString, defectIssuesId);
        }

        if (support > 0) {
            toString.append(support);
            toString.append(";");
        }
        toString.append(futureDefects);
        return toString.toString();
    }

    private void appendInteger(StringBuilder toString, Integer integer) {
        toString.append(integer).append(";");
    }

    private void appendSetInteger(StringBuilder toString, Set<Integer> set) {
        boolean appendComma = false;
        for (Integer integer : set) {
            if (appendComma) {
                toString.append(",");
            }
            toString.append(integer);
            appendComma = true;
        }
        toString.append(";");
    }
}
