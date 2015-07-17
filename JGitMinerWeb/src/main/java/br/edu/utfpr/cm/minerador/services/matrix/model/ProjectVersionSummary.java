package br.edu.utfpr.cm.minerador.services.matrix.model;

import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectVersionSummary {

    private final ProjectVersion projectVersion;
    private final Set<Issue> issues;
    private final Set<Commit> commits;
    private final Set<FilePair> filePairs;
    private final Set<FilePair> filePairsWithAtLeastTwoOccurrencesInAnyVersion;
    private final FilePairAprioriStatistics filePairsAprioriStatistics;

    public ProjectVersionSummary(ProjectVersion projectVersion) {
        this.projectVersion = projectVersion;
        this.issues = new LinkedHashSet<>();
        this.commits = new LinkedHashSet<>();
        this.filePairs = new LinkedHashSet<>();
        this.filePairsWithAtLeastTwoOccurrencesInAnyVersion = new LinkedHashSet<>();
        this.filePairsAprioriStatistics = new FilePairAprioriStatistics();
    }

    public ProjectVersionSummary(ProjectVersion projectVersion, Set<FilterByApriori> filters) {
        this.projectVersion = projectVersion;
        this.issues = new LinkedHashSet<>();
        this.commits = new LinkedHashSet<>();
        this.filePairs = new LinkedHashSet<>();
        this.filePairsWithAtLeastTwoOccurrencesInAnyVersion = new LinkedHashSet<>();
        this.filePairsAprioriStatistics = new FilePairAprioriStatistics(filters);
    }

    public ProjectVersion getProjectVersion() {
        return projectVersion;
    }

    public boolean addIssue(Issue issue) {
        return issues.add(issue);
    }

    public boolean addIssue(Collection<Issue> issues) {
        return this.issues.addAll(issues);
    }

    public boolean addCommit(Commit commit) {
        return commits.add(commit);
    }

    public boolean addCommit(Collection<Commit> commits) {
        return this.commits.addAll(commits);
    }

    public boolean addFilePair(FilePair filePair) {
        return filePairs.add(filePair);
    }

    public boolean addFilePair(Collection<FilePair> filePairs) {
        return this.filePairs.addAll(filePairs);
    }

    public boolean addFilePairWithAtLeastTwoOccurrencesInAnyVersion(FilePair filePair) {
        return filePairsWithAtLeastTwoOccurrencesInAnyVersion.add(filePair);
    }

    public boolean addFilePairApriori(FilePairApriori filePairApriori) {
        return filePairsAprioriStatistics.addFilePairApriori(filePairApriori);
    }

    public boolean addFilePairApriori(Collection<FilePairApriori> filePairsApriori) {
        return this.filePairsAprioriStatistics.addFilePairApriori(filePairsApriori);
    }

    public int issuesSize() {
        return issues.size();
    }

    public int commitsSize() {
        return commits.size();
    }

    public int filePairsSize() {
        return filePairs.size();
    }

    public FilePairAprioriStatistics getFilePairsAprioriStatistics() {
        return filePairsAprioriStatistics;
    }

    public Set<FilePair> getFilePairs() {
        return filePairs;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.projectVersion);
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
        final ProjectVersionSummary other = (ProjectVersionSummary) obj;
        if (!Objects.equals(this.projectVersion, other.projectVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(projectVersion.toString()).append(";")
                .append(issues.size()).append(";")
                .append(commits.size()).append(";")
                .append(filePairs.size()).append(";")
                .append(filePairsWithAtLeastTwoOccurrencesInAnyVersion.size()).append(";")
                .append(filePairsAprioriStatistics).toString();
    }

    public static String getHeader() {
        return "Project;Version;Issues;Commits;Cochange>=1;Cochange>=2;" + FilePairAprioriStatistics.getHeader();
    }
}
