package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A file pair per issue metrics
 *
 * @author Rodrigo Kuroda
 */
public class FilePairIssueMetrics extends FilePairMetrics {

    private static final String HEADER;
    private static final Map<String, Integer> headerIndexes;
    private static final Integer futureDefectsIndex;

    static {
        HEADER = "file;file2;issue;"
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

        String[] headerNames = HEADER.split(";");
        headerIndexes = new LinkedHashMap<>();
        for (int i = 0; i < headerNames.length; i++) {
            headerIndexes.put(headerNames[i], i);
        }

        futureDefectsIndex = headerIndexes.get("futureDefects");
    }

    private final FilePairIssue filePairIssue;
    private final IssueMetrics issueMetrics;

    public FilePairIssueMetrics(String file, String file2, IssueMetrics issueMetrics, double... metrics) {
        super(file, file2, metrics);
        this.issueMetrics = issueMetrics;
        this.filePairIssue = new FilePairIssue(getFilePair(), issueMetrics.getIssueNumber());
    }

    public FilePairIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        super(file, file2, metrics);
        this.issueMetrics = new EmptyIssueMetrics();
        this.filePairIssue = new FilePairIssue(getFilePair(), issue);
    }

    public FilePairIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        super(file, file2, metrics);
        this.issueMetrics = new EmptyIssueMetrics();
        this.filePairIssue = new FilePairIssue(getFilePair(), issue);
    }

    public String getHeader() {
        return HEADER;
    }

    public FilePairIssue getFilePairIssue() {
        return filePairIssue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(filePairIssue).append(";").append(issueMetrics);

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
        hash = 89 * hash + Objects.hashCode(filePairIssue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FilePairIssueMetrics)) {
            return false;
        }
        final FilePairIssueMetrics other = (FilePairIssueMetrics) obj;
        return Objects.equals(filePairIssue, other);
    }
}
