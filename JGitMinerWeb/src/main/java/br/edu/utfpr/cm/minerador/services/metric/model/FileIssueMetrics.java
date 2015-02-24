package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class FileIssueMetrics extends FileMetrics {

    public static final String HEADER;
    public static final Map<String, Integer> headerIndexes;
    public static final Integer futureDefectsIndex;

    static {
        HEADER = "file;file2;"
                // metricas da issue
                + IssueMetrics.HEADER
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
                // metricas de commit
                + "majorContributors;"
                + "oexp;"
                + "own;"
                + "files;" // total de arquivos modificados no commit
                + "committers;" // committers na release
                + "totalCommitters;" // committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos na release
                + "totalCommits;" // todos commits do par de arquivos
                + "devCommenters;" // número de autores de comentários que são desenvolvedores
                + "add;del;changes;" // do arquivo, no commit de uma issue corrigida
                + "ageRelease;ageTotal;" // idade do arquivo em dias
                + "futureDefects;" // numero de defeitos da proxima versao
                + "futureIssues;" // numero de issues, numero de issues da proxima versao
                + "pairChanged;";

        String[] headerNames = HEADER.split(";");
        headerIndexes = new LinkedHashMap<>();
        for (int i = 0; i < headerNames.length; i++) {
            headerIndexes.put(headerNames[i], i);
        }

        futureDefectsIndex = headerIndexes.get("futureDefects");
    }

    private final FileIssue fileIssue;
    private final String file2;
    private final IssueMetrics issueMetrics;

    public FileIssueMetrics(String file, String file2, IssueMetrics issueMetrics, double... metrics) {
        super(file, metrics);
        this.issueMetrics = issueMetrics;
        this.file2 = file2;
        this.fileIssue = new FileIssue(getFile(), issueMetrics.getIssueNumber());
    }

    public FileIssueMetrics(String file, String file2, IssueMetrics issueMetrics, Integer commit, double... metrics) {
        super(file, metrics);
        this.issueMetrics = issueMetrics;
        this.file2 = file2;
        this.fileIssue = new FileIssueCommit(getFile(), issueMetrics.getIssueNumber(), commit);
    }

    public FileIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        super(file, metrics);
        this.issueMetrics = new EmptyIssueMetrics();
        this.file2 = file2;
        this.fileIssue = new FileIssue(getFile(), issue);
    }

    public FileIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        super(file, metrics);
        this.issueMetrics = new EmptyIssueMetrics();
        this.file2 = file2;
        this.fileIssue = new FileIssue(getFile(), issue);
    }

    public String getHeader() {
        return HEADER;
    }

    public FileIssue getFileIssue() {
        return fileIssue;
    }

    public IssueMetrics getIssueMetrics() {
        return issueMetrics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fileIssue).append(";").append(file2)
                .append(";").append(issueMetrics);

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
        hash = 89 * hash + fileIssue.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileIssueMetrics)) {
            return false;
        }
        final FileIssueMetrics other = (FileIssueMetrics) obj;
        return Objects.equals(fileIssue, other.fileIssue);
    }
}
