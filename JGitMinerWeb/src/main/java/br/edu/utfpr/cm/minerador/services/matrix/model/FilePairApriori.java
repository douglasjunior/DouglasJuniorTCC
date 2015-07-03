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
    private final boolean file2HasGreaterConfidence;
    private final double highestConfidence;

    public FilePairApriori(long fileIssues, long file2Issues, long filePairIssues, long allIssues) {
        this.fileIssues = fileIssues;
        this.file2Issues = file2Issues;
        this.issues = filePairIssues;
        this.allIssues = allIssues;

        supportFile = this.fileIssues / (double) allIssues;
        supportFile2 = this.file2Issues / (double) allIssues;
        supportFilePair = filePairIssues / (double) allIssues;
        confidence = supportFile == 0 ? 0d : supportFilePair / supportFile;
        confidence2 = supportFile2 == 0 ? 0d : supportFilePair / supportFile2;
        lift = supportFile * supportFile2 == 0 ? 0d : supportFilePair / (supportFile * supportFile2);
        conviction = 1 - confidence == 0 ? 0d : (1 - supportFile) / (1 - confidence);
        conviction2 = 1 - confidence2 == 0 ? 0d : (1 - supportFile2) / (1 - confidence2);

        if (confidence2 > confidence) {
            this.file2HasGreaterConfidence = true;
            this.highestConfidence = confidence2;
        } else {
            this.file2HasGreaterConfidence = false;
            this.highestConfidence = confidence;
        }
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

    public boolean isFile2GreaterConfidence() {
        return file2HasGreaterConfidence;
    }

    public double getHighestConfidence() {
        return highestConfidence;
    }

    public boolean hasMinIssues(int minIssues) {
        return minIssues <= issues;
    }

    public boolean hasMaxIssues(int maxIssues) {
        return maxIssues >= issues;
    }

    public boolean hasMinSupport(Double minSupport) {
        return minSupport <= supportFilePair;
    }

    public boolean hasMaxSupport(Double maxSupport) {
        return maxSupport >= supportFilePair;
    }

    public boolean hasMinConfidence(Double minConfidence) {
        return minConfidence <= highestConfidence;
    }

    public boolean hasMaxConfidence(Double maxConfidence) {
        return maxConfidence >= highestConfidence;
    }

    public boolean fits(FilterByApriori aprioriFilter) {
        return hasMinMaxConfidence(aprioriFilter.getMinConfidence(), aprioriFilter.getMaxConfidence())
                && hasMinMaxSupport(aprioriFilter.getMinSupport(), aprioriFilter.getMaxSupport())
                && hasMinMaxIssues(aprioriFilter.getMinIssues(), aprioriFilter.getMaxIssues());
    }

    /**
     * Returns true if: 1) min <= confidence <= max; or 2) min is null and
     * issues <= max; or 3) max is null and min <= issues; or 4) min and max are
     * null.
     *
     * @param minConfidence
     * @param maxConfidence
     * @return
     */
    public boolean hasMinMaxConfidence(Double minConfidence, Double maxConfidence) {
        if (minConfidence != null && maxConfidence != null) {
            return hasMinConfidence(minConfidence) && hasMaxConfidence(maxConfidence);
        } else if (minConfidence != null) {
            return hasMinConfidence(minConfidence);
        } else if (maxConfidence != null) {
            return hasMaxConfidence(maxConfidence);
        }
        return true;
    }

    /**
     * Returns true if: 1) min <= support <= max; or 2) min is null and issues
     * <= max; or 3) max is null and min <= issues; or 4) min and max are null.
     *
     * @param minSupport
     * @param maxSupport
     * @return
     */
    public boolean hasMinMaxSupport(Double minSupport, Double maxSupport) {
        if (minSupport != null && maxSupport != null) {
            return hasMinSupport(minSupport) && hasMaxSupport(maxSupport);
        } else if (minSupport != null) {
            return hasMinSupport(minSupport);
        } else if (maxSupport != null) {
            return hasMaxSupport(maxSupport);
        }
        return true;
    }

    /**
     * Returns true if: 1) min <= issues <= max; or 2) min is null and issues <=
     * max; or 3) max is null and min <= issues; or 4) min and max are null.
     *
     * @param minIssues
     * @param maxIssues
     * @return
     */
    public boolean hasMinMaxIssues(Integer minIssues, Integer maxIssues) {
        if (minIssues != null && maxIssues != null) {
            return hasMinIssues(minIssues) && hasMaxIssues(maxIssues);
        } else if (minIssues != null) {
            return hasMinIssues(minIssues);
        } else if (maxIssues != null) {
            return hasMaxIssues(maxIssues);
        }
        return true;
    }

    @Override
    public String toString() {
        if (file2HasGreaterConfidence) {
            return file2Issues + SEPARATOR
                    + fileIssues + SEPARATOR
                    + issues + SEPARATOR
                    + allIssues + SEPARATOR
                    + supportFile2 + SEPARATOR
                    + supportFile + SEPARATOR
                    + supportFilePair + SEPARATOR
                    + confidence2 + SEPARATOR
                    + confidence + SEPARATOR
                    + lift + SEPARATOR
                    + conviction2 + SEPARATOR
                    + conviction + SEPARATOR;
        }
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

    public String toStringPairFileApriori() {

        if (file2HasGreaterConfidence) {
            return supportFilePair + SEPARATOR
                    + confidence2 + SEPARATOR;
        }
        return supportFilePair + SEPARATOR
                + confidence + SEPARATOR;
    }

    public static String getToStringHeader() {
        return "fileIssues;file2Issues;issues;allIssues;supportFile;supportFile2;"
                + "supportFilePair;confidence;confidence2;lift;conviction;conviction2;";
    }

    public static String getToStringHeaderPairFileApriori() {
        return "supportFilePair;confidenceFilePair;";
    }
}
