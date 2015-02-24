package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.minerador.services.metric.model.IssueMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFileIssue;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFileIssueMetrics extends AuxFileFileMetrics {

    private final String header;
    private final Map<String, Integer> headerIndexes;
    private final Integer futureDefectsIndex;

    {
        header = "file;file2;issue;"
                + "issueType;issuePriority;issueAssignedTo;issueSubmittedBy;"
                + "issueWatchers;"
                + "issueReopened;" // quantidade em que foi reaberto (status = reopened)
                + "samePackage;" // arquivos do par são do mesmo pacote = 1, caso contrário 0
                + "sameOwnership;" // mesmo autor que fez o commit do par na issue analisada e no ultimo commit antes da issue
                // + "brcAvg;brcSum;brcMax;"
                + "btwSum;btwAvg;btwMdn;btwMax;"
                + "clsSum;clsAvg;clsMdn;clsMax;"
                + "dgrSum;dgrAvg;dgrMdn;dgrMax;"
                //+ "egvSum;egvAvg;egvMax;"
                + "egoBtwSum;egoBtwAvg;egoBtwMdn;egoBtwMax;"
                + "egoSizeSum;egoSizeAvg;egoSizeMdn;egoSizeMax;"
                + "egoTiesSum;egoTiesAvg;egoTiesMdn;egoTiesMax;"
                // + "egoPairsSum;egoPairsAvg;egoPairsMax;"
                + "egoDensitySum;egoDensityAvg;egoDensityMdn;egoDensityMax;"
                + "efficiencySum;efficiencyAvg;efficiencyMdn;efficiencyMax;"
                + "efvSizeSum;efvSizeAvg;efvSizeMdn;efvSizeMax;"
                + "constraintSum;constraintAvg;constraintMdn;constraintMax;"
                + "hierarchySum;hierarchyAvg;hierarchyMdn;hierarchyMax;"
                + "size;ties;density;diameter;"
                + "devCommitsSum;devCommitsAvg;devCommitsMdn;devCommitsMax;"
                + "ownershipSum;ownershipAvg;ownershipMdn;ownershipMax;"
                + "majorContributors;minorContributors;"
                + "oexp;oexp2;"
                + "own;own2;"
                + "files;" // total de arquivos modificados na issue
                + "committers;" // committers na release
                + "totalCommitters;" // committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos na release
                + "totalCommits;" // todos commits do par de arquivos
                + "devCommenters;" // número de autores de comentários que são desenvolvedores
                + "commenters;comments;wordiness;"
                + "codeChurn;codeChurn2;codeChurnAvg;"
                + "add;del;changes;" // do par, até a release analisada com base na data de resolução (fix date)
                // + "rigidityFile1;rigidityFile2;rigidityPairFile;"
                // + "taskImprovement;taskDefect;"
                + "futureDefects;" // numero de defeitos da proxima versao
                + "ageRelease;ageTotal;"
                + "updates;futureUpdates;" // numero de issues, numero de issues da proxima versao
                + "fileIssues;file2Issues;allIssues;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2;changed";

        String[] headerNames = header.split(";");
        headerIndexes = new LinkedHashMap<>();
        for (int i = 0; i < headerNames.length; i++) {
            headerIndexes.put(headerNames[i], i);
        }

        futureDefectsIndex = headerIndexes.get("futureDefects");
    }

    private final AuxFileFileIssue fileFileIssue;
    private final Integer issue;
    private final String issueKey;
    private final String issueType;
    private final String issuePriority;
    private final String issueAssignedTo;
    private final String issueSubmittedBy;
    private final long numberOfWatchers;

    public AuxFileFileIssueMetrics(String file, String file2, IssueMetrics issueMetrics, double... metrics) {
        super(file, file2, metrics);
        this.issue = issueMetrics.getIssueNumber();
        this.issueKey = issueMetrics.getIssueKey();
        this.issueType = issueMetrics.getIssueType();
        this.issuePriority = issueMetrics.getPriority();
        this.issueAssignedTo = issueMetrics.getAssignedTo();
        this.issueSubmittedBy = issueMetrics.getSubmittedBy();
        this.numberOfWatchers = issueMetrics.getNumberOfWatchers();
        this.fileFileIssue = new AuxFileFileIssue(getFileFile(), issue);
    }

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        super(file, file2, metrics);
        this.issue = issue;
        this.issueKey = "";
        this.issueType = "";
        this.issuePriority = "";
        this.issueAssignedTo = "";
        this.issueSubmittedBy = "";
        this.numberOfWatchers = 0;
        this.fileFileIssue = new AuxFileFileIssue(getFileFile(), issue);
    }

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        super(file, file2, metrics);
        this.issue = issue;
        this.issueKey = "";
        this.issueType = "";
        this.issuePriority = "";
        this.issueAssignedTo = "";
        this.issueSubmittedBy = "";
        this.numberOfWatchers = 0;
        this.fileFileIssue = new AuxFileFileIssue(getFileFile(), issue);
    }

    public String getHeader() {
        return header;
    }

    public Integer getIssue() {
        return issue;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getIssuePriority() {
        return issuePriority;
    }

    public String getIssueAssignedTo() {
        return issueAssignedTo;
    }

    public String getIssueSubmittedBy() {
        return issueSubmittedBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getFile());
        sb.append(";").append(getFile2())
                .append(";").append(issueKey)
                .append(";").append(issueType)
                .append(";").append(issuePriority)
                .append(";").append(issueAssignedTo)
                .append(";").append(issueSubmittedBy)
                .append(";").append(numberOfWatchers);

        for (double m : getMetrics()) {
            sb.append(";");
            sb.append(m);
        }
        sb.append(";").append(getRisky());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (Objects.hashCode(getFile()) + Objects.hashCode(getFile2()));
        hash = 89 * hash + Objects.hashCode(this.issue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AuxFileFileIssueMetrics)) {
            return false;
        }
        final AuxFileFileIssueMetrics other = (AuxFileFileIssueMetrics) obj;
        if (Objects.equals(issue, other.issue)) {
            if (Util.stringEquals(getFile(), other.getFile())
                    && Util.stringEquals(getFile2(), other.getFile2())) {
                return true;
            }
            if (Util.stringEquals(getFile(), other.getFile2())
                    && Util.stringEquals(getFile2(), other.getFile())) {
                return true;
            }
        }
        return false;
    }
}
