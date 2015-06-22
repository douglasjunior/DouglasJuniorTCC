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

    public ProjectVersionSummary(ProjectVersion projectVersion) {
        this.projectVersion = projectVersion;
        this.issues = new LinkedHashSet<>();
        this.commits = new LinkedHashSet<>();
        this.filePairs = new LinkedHashSet<>();
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

    public int issuesSize() {
        return issues.size();
    }

    public int commitsSize() {
        return commits.size();
    }

    public int filePairsSize() {
        return filePairs.size();
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
        return projectVersion.toString() + ";" + issues.size() + ";" + commits.size() + ";" + filePairs.size();
    }
}
