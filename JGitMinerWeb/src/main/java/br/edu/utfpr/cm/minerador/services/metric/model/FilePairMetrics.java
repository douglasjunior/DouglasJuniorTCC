package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A file pair metrics.
 *
 * @author Rodrigo Kuroda
 */
public class FilePairMetrics {

    public static final Map<String, Integer> HEADER_INDEX;
    public static final Integer futureDefectsIndex;

    static {
        String[] headerNames = ("file;file2;"
                + "samePackage;" // arquivos do par são do mesmo pacote = 1, caso contrário 0
                // + "brcAvg;brcSum;brcMax;"
                + "btwSum;btwAvg;btwMdn;btwMax;"
                + "clsSum;clsAvg;clsMdn;clsMax;"
                + "dgrSum;dgrAvg;dgrMdn;dgrMax;"
                //+ "egvSum;egvAvg;egvMax;"
                + "egoBtwSum;egoBtwAvg;egoBtwMdn;egoBtwMax;"
                + "egoSizeSum;egoSizeAvg;egoSizeMdn;egoSizeMax;"
                + "egoTiesSum;egoTiesAvg;egoTiesMdn;egoTiesMax;"
                // + "egoPairsSum;egoPairsAvg;egoPairsMax;"
                + "egoDensitySum;egoDensityAvg;egoDensityMdn;egoDensityMax;"
                + "efficiencySum;efficiencyAvg;efficiencyMdn;efficiencyMax;"
                + "efvSizeSum;efvSizeAvg;efvSizeMdn;efvSizeMax;"
                + "constraintSum;constraintAvg;constraintMdn;constraintMax;"
                + "hierarchySum;hierarchyAvg;hierarchyMdn;hierarchyMax;"
                + "size;ties;density;diameter;"
                + "devCommitsSum;devCommitsAvg;devCommitsMdn;devCommitsMax;"
                + "ownershipSum;ownershipAvg;ownershipMdn;ownershipMax;"
                + "majorContributors;minorContributors;"
                + "oexp;oexp2;"
                + "own;own2;"
                + "adev;" // committers na release
                + "ddev;" // committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos
                + "devCommenters;" // número de autores de comentários que são desenvolvedores
                + "commenters;comments;wordiness;"
                + "codeChurn;codeChurn2;codeChurnAvg;"
                + "add;del;changes;"
                //                + "rigidityFile1;rigidityFile2;rigidityPairFile;"
                + "taskImprovement;taskDefect;futureDefects;"
                + "ageRelease;ageTotal;"
                + "updates;futureUpdates;"
                + "fileFutureIssues;file2FutureIssues;allFutureIssues;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2;changed").split(";");
        Map<String, Integer> headerIndex = new LinkedHashMap<>();
        int index = 0;
        for (String headerName : headerNames) {
            headerIndex.put(headerName, index++);
        }
        HEADER_INDEX = Collections.unmodifiableMap(headerIndex);
        futureDefectsIndex = HEADER_INDEX.get("futureDefects");
    }

    private final FilePair filePair;
    private FilePairApriori filePairApriori;
    private final List<Double> metrics;
    private int changed = 0;

    public FilePairMetrics(String file, String file2, double... metrics) {
        this.filePair = new FilePair(file, file2);
        this.metrics = new ArrayList<>();
        addMetrics(metrics);
    }

    public FilePairMetrics(FilePair filePair, double... metrics) {
        this.filePair = filePair;
        this.metrics = new ArrayList<>();
        addMetrics(metrics);
    }

    public FilePairMetrics(String file, String file2, List<Double> metrics) {
        this.filePair = new FilePair(file, file2);
        this.metrics = metrics;
    }

    public FilePairMetrics(FilePair filePair, List<Double> metrics) {
        this.filePair = filePair;
        this.metrics = metrics;
    }

    public FilePairApriori getFilePairApriori() {
        return filePairApriori;
    }

    public void setFilePairApriori(FilePairApriori filePairApriori) {
        this.filePairApriori = filePairApriori;
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public List<Double> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(filePair);
        for (double measure : metrics) {
            sb.append(";");
            sb.append(measure);
        }
        sb.append(";");
        sb.append(changed);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(filePair);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FilePairMetrics)) {
            return false;
        }
        final FilePairMetrics other = (FilePairMetrics) obj;
        return Objects.equals(filePair, other.filePair);
    }

    public void addMetrics(double... metrics) {
        for (double value : metrics) {
            this.metrics.add(value);
        }
    }

    public void changed() {
        changed = 1;
    }

    public int getRisky() {
        return changed;
    }

    public int getFutureDefectIssuesIdWeight() {
        return metrics.get(futureDefectsIndex).intValue();
    }

}
