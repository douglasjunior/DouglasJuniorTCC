package br.edu.utfpr.cm.JGitMinerWeb.services.metric.commit;

/**
 * ___1_2_3_4_5_6_7__________________________________________
 * A|*|*| | | | | | | | = 0.1 ...............................
 * B| | | |*| | | | | | = 0.1 ...............................
 * C|*| | | | |*| |*| | === 0.3 .............................
 * D|*|*| |*|*|*|*| | | ===== 0.5 ...........................
 * ...^-------------^........................................
 * ...analisys.period.......................................
 *
 * @author Rodrigo T. Kuroda
 */
public class EntropyCalculator {
    private final int totalCommits;

    public EntropyCalculator(final int totalCommits) {
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
