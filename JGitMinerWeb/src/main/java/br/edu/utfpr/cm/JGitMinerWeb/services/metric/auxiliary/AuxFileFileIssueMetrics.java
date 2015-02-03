package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxFileFileIssueMetrics extends AuxFileFileMetrics {
    public static final Map<String, Integer> HEADER_INDEX;
    public static final Integer futureDefectsIndex;

    static {
        String[] headerNames = ("file;file2;issue;"
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

    private final Integer issue;

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, double... metrics) {
        super(file, file2, metrics);
        this.issue = issue;
    }

    public AuxFileFileIssueMetrics(String file, String file2, Integer issue, List<Double> metrics) {
        super(file, file2, metrics);
        this.issue = issue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getFile());
        sb.append(";").append(getFile2()).append(";").append(issue);
        for (double m : getMetrics()) {
            sb.append(";");
            sb.append(Util.tratarDoubleParaString(m));
        }
        sb.append(";").append(getRisky());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (Objects.hashCode(getFile()) + Objects.hashCode(getFile2()));
        hash = 89 * hash + Objects.hashCode(this.issue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AuxFileFileIssueMetrics)) {
            return false;
        }
        final AuxFileFileIssueMetrics other = (AuxFileFileIssueMetrics) obj;
        if (Objects.equals(issue, other.issue)) {
            if (Util.stringEquals(getFile(), other.getFile())
                    && Util.stringEquals(getFile2(), other.getFile2())) {
                return true;
            }
            if (Util.stringEquals(getFile(), other.getFile2())
                    && Util.stringEquals(getFile2(), other.getFile())) {
                return true;
            }
        }
        return false;
    }
}
