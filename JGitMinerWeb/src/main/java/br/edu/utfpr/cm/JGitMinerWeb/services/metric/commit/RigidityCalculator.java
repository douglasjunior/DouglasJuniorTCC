package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class RigidityCalculator {

    private final int totalCommits;

    public RigidityCalculator(final int totalCommits) {
        this.totalCommits = totalCommits;
    }

    public int getTotalCommits() {
        return totalCommits;
    }

    public double calcule(final int commits) {
        return (double) commits / totalCommits;
    }

    public double calcule(final int commitsFile1, final int commitsFile2) {
        int commits = commitsFile1 + commitsFile2;
        return (double) commits / totalCommits;
    }
}
