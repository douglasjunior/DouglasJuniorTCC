package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author douglas
 */
public class AuxFileFile {

    private final String fileName;
    private final String fileName2;
    private final Set<Integer> issuesId;
    private final Set<Integer> commitsId;
    private final Set<Integer> defectIssuesId;
    private int issuesWeight = 0;
    private int commitsWeight = 0;
    private int defectsWeight = 0;
    private long futureDefects = 0;
    private double support = 0;

    public AuxFileFile(String fileName, String fileName2) {
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.issuesId = new HashSet<>();
        this.commitsId = new HashSet<>();
        this.defectIssuesId = new HashSet<>();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileName2() {
        return fileName2;
    }

    public Set<Integer> getIssuesId() {
        return Collections.unmodifiableSet(issuesId);
    }

    public Set<Integer> getCommitsId() {
        return commitsId;
    }

    public Set<Integer> getDefectIssuesId() {
        return defectIssuesId;
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
        if (obj != null && obj instanceof AuxFileFile) {
            AuxFileFile other = (AuxFileFile) obj;
            if (Util.stringEquals(this.fileName, other.fileName)
                    && Util.stringEquals(this.fileName2, other.fileName2)) {
                return true;
            }
            if (Util.stringEquals(this.fileName, other.fileName2)
                    && Util.stringEquals(this.fileName2, other.fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash +
                (Objects.hashCode(this.fileName) + Objects.hashCode(this.fileName2));
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
        toString.append(fileName).append(";").append(fileName2).append(";");

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
