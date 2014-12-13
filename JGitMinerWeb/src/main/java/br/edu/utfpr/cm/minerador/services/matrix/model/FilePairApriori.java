package br.edu.utfpr.cm.minerador.services.matrix.model;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairApriori {

    private static final String SEPARATOR = ";";

    private final long fileIssues;
    private final long file2Issues;
    private final long issues;
    private final long allIssues;
    private final double supportFile;
    private final double supportFile2;
    private final double supportFilePair;
    private final double confidence;
    private final double confidence2;
    private final double lift;
    private final double conviction;
    private final double conviction2;

    public FilePairApriori(long fileIssues, long file2Issues, long filePairIssues, long allIssues) {
        this.fileIssues = fileIssues;
        this.file2Issues = file2Issues;
        this.issues = filePairIssues;
        this.allIssues = allIssues;

        supportFile = fileIssues / (double) filePairIssues;
        supportFile2 = file2Issues / (double) filePairIssues;
        supportFilePair = filePairIssues / (double) allIssues;
        confidence = supportFile == 0 ? 0d : supportFilePair / supportFile;
        confidence2 = supportFile2 == 0 ? 0d : supportFilePair / supportFile2;
        lift = supportFile * supportFile2 == 0 ? 0d : supportFilePair / (supportFile * supportFile2);
        conviction = 1 - confidence == 0 ? 0d : (1 - supportFile) / (1 - confidence);
        conviction2 = 1 - confidence2 == 0 ? 0d : (1 - supportFile2) / (1 - confidence2);
    }

    public long getFileIssues() {
        return fileIssues;
    }

    public long getFile2Issues() {
        return file2Issues;
    }

    public long getIssues() {
        return issues;
    }

    public long getAllIssues() {
        return allIssues;
    }

    public double getSupportFile() {
        return supportFile;
    }

    public double getSupportFile2() {
        return supportFile2;
    }

    public double getSupportFilePair() {
        return supportFilePair;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getConfidence2() {
        return confidence2;
    }

    public double getLift() {
        return lift;
    }

    public double getConviction() {
        return conviction;
    }

    public double getConviction2() {
        return conviction2;
    }

    @Override
    public String toString() {
        return fileIssues + SEPARATOR
                + file2Issues + SEPARATOR
                + issues + SEPARATOR
                + allIssues + SEPARATOR
                + supportFile + SEPARATOR
                + supportFile2 + SEPARATOR
                + supportFilePair + SEPARATOR
                + confidence + SEPARATOR
                + confidence2 + SEPARATOR
                + lift + SEPARATOR
                + conviction + SEPARATOR
                + conviction2 + SEPARATOR;
    }

    public static String getToStringHeader() {
        return "fileIssues;file2Issues;issues;allIssues;supportFile;supportFile2;"
                + "supportFilePair;confidence;confidence2;lift;conviction;conviction2;";
    }
}