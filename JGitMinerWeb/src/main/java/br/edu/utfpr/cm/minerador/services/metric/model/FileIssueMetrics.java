package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.minerador.services.metric.committer.CommitterFileMetrics;
import br.edu.utfpr.cm.minerador.services.metric.socialnetwork.NetworkMetrics;
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
            = "file1;file2;"
            // metricas da issue
            + IssueMetrics.HEADER
            + NetworkMetrics.HEADER
            + CommitMetrics.HEADER
            + CommitterFileMetrics.HEADER
            // metricas de commit
            + "isMajorContributor;" // committer é principal colaborador do arquivo
            + "ownerExperience;" // experiencia do owner na versao anterior
            + "sameOwnership;" // total de arquivos modificados no commit
            + "files;" // total de arquivos modificados no commit
//            + "committers;" // committers na release
            + "pv_totalCommitters;" // committers desde o começo ate a data final da relese
//            + "commits;" // commits do par de arquivos na release
            + "pv_totalCommits;" // todos commits do arquivo
            + "addedLines;deletedLines;changedLines;" // do arquivo, no commit de uma issue corrigida
            + "fileAge;" // idade do arquivo na versão em dias na versao em analise
            + "pv_totalFileAge;" // idade do arquivo em dias desde o primeiro commit
            + "futureDefects;" // numero de defeitos do primeiro arquivo na proxima versao
            + "futureIssues;" // numero de issues do arquivo na proxima versao
            + "isFilePairChanged;" // o par mudou nesse commit? 0 = não, 1 = sim
            + "changedAfterReopened" // index (1a reabertura, 2a, 3a e assim sucessivamente) onde o arquivo B foi alterado após a issue ter sido reaberta
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
    private CommitterFileMetrics committerFileMetrics;
    private int changedAfterReopened;

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

    public CommitterFileMetrics getCommitterFileMetrics() {
        return committerFileMetrics;
    }

    public void setCommitterFileMetrics(CommitterFileMetrics committerFileMetrics) {
        this.committerFileMetrics = committerFileMetrics;
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

    public int getChangedAfterReopened() {
        return changedAfterReopened;
    }

    public void setFileBChangedAfterReopened(int reopenedIndex) {
        this.changedAfterReopened = reopenedIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fileIssue)
                .append(file2).append(";")
                .append(issueMetrics)
                .append(networkMetrics)
                .append(commitMetrics)
                .append(committerFileMetrics);

        for (double m : getMetrics()) {
            sb.append(m).append(";");
        }
        sb.append(getChanged());
        sb.append(";").append(changedAfterReopened);
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
