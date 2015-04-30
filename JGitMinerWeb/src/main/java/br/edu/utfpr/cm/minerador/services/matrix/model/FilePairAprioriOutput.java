package br.edu.utfpr.cm.minerador.services.matrix.model;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairAprioriOutput extends FilePairOutput {

    private FilePairApriori filePairApriori;

    public FilePairAprioriOutput(FilePair filePair) {
        super(filePair);
    }

    public FilePairApriori getFilePairApriori() {
        return filePairApriori;
    }

    public void setFilePairApriori(FilePairApriori filePairApriori) {
        this.filePairApriori = filePairApriori;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();

        toString.append(filePair.toString(filePairApriori));

        appendInteger(toString, issuesId.size());
        appendSetInteger(toString, issuesId);

        appendInteger(toString, commitsId.size());
        appendSetInteger(toString, commitsId);

        appendInteger(toString, commitsFile1Id.size());
        appendSetInteger(toString, commitsFile1Id);

        appendInteger(toString, commitsFile2Id.size());
        appendSetInteger(toString, commitsFile2Id);

        appendInteger(toString, defectIssuesId.size());
        appendSetInteger(toString, defectIssuesId);

        appendInteger(toString, futureDefectIssuesId.size());
        appendSetInteger(toString, futureDefectIssuesId);

        appendInteger(toString, futureIssuesId.size());
        appendSetInteger(toString, futureIssuesId);

        toString.append(filePairApriori.toString());

        return toString.toString();
    }
    public static String getToStringHeader() {
        return FilePair.getToStringHeader()
                + "issues;issuesId;"
                + "commits;commitsId;"
                + "commitsFile1;commitsFile1Id;"
                + "commitsFile2;commitsFile2Id;"
                + "defectIssues;defectIssuesId;"
                + "futureDefectIssues;futureDefectIssuesId;"
                + "futureIssues;futureIssuesId;"
                + FilePairApriori.getToStringHeader();
    }
}
