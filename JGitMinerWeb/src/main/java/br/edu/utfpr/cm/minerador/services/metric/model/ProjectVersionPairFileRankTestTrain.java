package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.minerador.services.matrix.model.Project;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectVersionPairFileRankTestTrain {

    private final Project project;
    private final FilePair filePair;
    private final int rank;
    private final Set<Integer> issuesTrain;
    private final Set<Integer> commitsTrain;
    private final Set<Integer> issuesTest;
    private final Set<Integer> commitsTest;

    public ProjectVersionPairFileRankTestTrain(Project project, FilePair filePair, int rank) {
        this.project = project;
        this.filePair = filePair;
        this.rank = rank;
        this.issuesTrain = new LinkedHashSet<>();
        this.commitsTrain = new LinkedHashSet<>();
        this.issuesTest = new LinkedHashSet<>();
        this.commitsTest = new LinkedHashSet<>();
    }

    public void addIssueCommitForTrain(Integer issue, Integer commit) {
        issuesTrain.add(issue);
        commitsTrain.add(commit);
    }

    public void addIssueCommitForTest(Integer issue, Integer commit) {
        issuesTest.add(issue);
        commitsTest.add(commit);
    }

    public Project getProject() {
        return project;
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public int getRank() {
        return rank;
    }

    public Set<Integer> getIssuesTrain() {
        return issuesTrain;
    }

    public Set<Integer> getCommitsTrain() {
        return commitsTrain;
    }

    public Set<Integer> getIssuesTest() {
        return issuesTest;
    }

    public Set<Integer> getCommitsTest() {
        return commitsTest;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.project);
        hash = 41 * hash + Objects.hashCode(this.filePair);
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
        final ProjectVersionPairFileRankTestTrain other = (ProjectVersionPairFileRankTestTrain) obj;
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        if (!Objects.equals(this.filePair, other.filePair)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return project + ", filePair=" + filePair + ", rank=" + rank + ", issuesTrain=" + issuesTrain + ", commitsTrain=" + commitsTrain + ", issuesTest=" + issuesTest + ", commitsTest=" + commitsTest + '}';
    }

}
