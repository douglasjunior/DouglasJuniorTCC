package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.minerador.services.metric.NetworkMetrics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class FileIssueMetrics extends FileMetrics {

    public static final String HEADER
            = "file;file2;"
            // metricas da issue
            + IssueMetrics.HEADER
            + NetworkMetrics.HEADER
            + CommitMetrics.HEADER
            + "sameOwnership;" // mesmo autor que fez o commit do par na issue analisada e no ultimo commit antes da issue
            // metricas de commit
            + "majorContributor;"
            + "oexp;"
            + "own;"
            + "files;" // total de arquivos modificados no commit
//            + "committers;" // committers na release
            + "totalCommitters;" // committers desde o começo até a data final da relese
//            + "commits;" // commits do par de arquivos na release
            + "totalCommits;" // todos commits do arquivo
            + "add;del;changes;" // do arquivo, no commit de uma issue corrigida
            + "ageRelease;ageTotal;" // idade do arquivo em dias
            + "futureDefects;" // numero de defeitos do primeiro arquivo na proxima versao
            + "futureIssues;" // numero de issues do arquivo na proxima versao
            + "pairChanged;" // o par mudou nesse commit? 0 = não, 1 = sim
            ;

    public static final Map<String, Integer> headerIndexes;
    public static final Integer futureDefectsIndex;

    static {
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
    private NetworkMetrics networkMetrics;
    private CommitMetrics commitMetrics;

    public FileIssueMetrics(String file, String file2, IssueMetrics issueMetrics, double... metrics) {
        super(file, metrics);
        this.issueMetrics = issueMetrics;
        this.file2 = file2;
        this.fileIssue = new FileIssue(getFile(), issueMetrics.getIssueNumber());
    }

    public FileIssueMetrics(String file, String file2, Commit commit, IssueMetrics issueMetrics, double... metrics) {
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

    public NetworkMetrics getNetworkMetrics() {
        return networkMetrics;
    }

    public void setNetworkMetrics(NetworkMetrics networkMetrics) {
        this.networkMetrics = networkMetrics;
    }

    public CommitMetrics getCommitMetrics() {
        return commitMetrics;
    }

    public void setCommitMetrics(CommitMetrics commitMetrics) {
        this.commitMetrics = commitMetrics;
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
        sb.append(fileIssue)
                .append(file2).append(";")
                .append(issueMetrics)
                .append(networkMetrics)
                .append(commitMetrics);

        for (double m : getMetrics()) {
            sb.append(m).append(";");
        }
        sb.append(getRisky());
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
