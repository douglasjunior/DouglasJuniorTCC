package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilterFilePairByReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Project;
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectFilePairReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.util.VersionUtil;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Report 3 - Projects Pairs of File Versions Occurrences
  *
 * @author Rodrigo Kuroda
 */
public class BichoProjectsFilePairReleaseOccurenceServices extends AbstractBichoMatrixServices {

    public BichoProjectsFilePairReleaseOccurenceServices() {
        super(null, null);
    }

    public BichoProjectsFilePairReleaseOccurenceServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoProjectsFilePairReleaseOccurenceServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private Integer getMaxFilesPerCommit() {
        return Util.stringToInteger(params.get("maxFilesPerCommit") + "");
    }

    private Integer getMinFilesPerCommit() {
        return Util.stringToInteger(params.get("minFilesPerCommit") + "");
    }

    public List<String> getFilesToIgnore() {
        return getStringLinesParam("filesToIgnore", true, false);
    }

    public List<String> getFilesToConsiders() {
        return getStringLinesParam("filesToConsiders", true, false);
    }

    private Double getMinSupport() {
        return Util.stringToDouble(params.get("minSupport") + "");
    }

    private Double getMaxSupport() {
        return Util.stringToDouble(params.get("maxSupport") + "");
    }

    private Double getMinConfidence() {
        return Util.stringToDouble(params.get("minConfidence") + "");
    }

    private Double getMaxConfidence() {
        return Util.stringToDouble(params.get("maxConfidence") + "");
    }

    private int getMinOccurencesInVersion() {
        return 1; //Util.stringToInteger(params.get("minOccurrencesInVersion") + "");
    }

    @Override
    public void run() {
        System.out.println(params);
        StringBuilder summaryRepositoryName = new StringBuilder();

        // TODO parametrizar
        // TODO confirmar/verificar de acordo com a planilha
        final List<FilterFilePairByReleaseOcurrence> filters = FilterFilePairByReleaseOcurrence.getSuggestedFilters();

        Double minSupport = getMinSupport();
        Double maxSupport = getMaxSupport();
        Double minConfidence = getMinConfidence();
        Double maxConfidence = getMaxConfidence();
        int minFilePairOccurrences = 1; //TODO parametrizar
        int minOccurrencesInVersion = getMinOccurencesInVersion();

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");
        final Set<ProjectFilePairReleaseOcurrence> releaseOccurrences = new LinkedHashSet<>();

        for (String project : getSelectedProjects()) {

            BichoDAO bichoDAO = new BichoDAO(dao, project, getMaxFilesPerCommit());
            BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, project, getMaxFilesPerCommit());
            BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, project, getMaxFilesPerCommit());

            final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();

            final ProjectFilePairReleaseOcurrence projectVersionFilePairReleaseOcurrence
                    = new ProjectFilePairReleaseOcurrence(new Project(project),
                            VersionUtil.listStringToListVersion(fixVersionOrdered),
                            minFilePairOccurrences, minOccurrencesInVersion, filters);

            for (String versionString : fixVersionOrdered) {
                Version version = new Version(versionString);

                out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
                out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

                final Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
                identifyFilePairs(pairFiles, bichoDAO, versionString, bichoFileDAO);

                if (pairFiles.isEmpty()) {
                    continue;
                }
                Set<Integer> allConsideredIssues = new HashSet<>();
                for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                    FilePairAprioriOutput value = entrySet.getValue();
                    allConsideredIssues.addAll(value.getIssuesId());
                }

                Cacher cacher = new Cacher(bichoFileDAO, bichoPairFileDAO);
                for (FilePair fileFile : pairFiles.keySet()) {
                    Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version.getVersion());
                    Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version.getVersion());

                    FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

                    FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                            filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

                    fileFile.orderFilePairByConfidence(apriori);
                    filePairOutput.setFilePairApriori(apriori);
                }

                for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                    FilePair filePair = entrySet.getKey();
                    FilePairAprioriOutput value = entrySet.getValue();
                    final FilePairApriori apriori = value.getFilePairApriori();
                    if (apriori.hasMinIssues(3)
                            && apriori.hasMinConfidence(0.9)) {
//                    if (apriori.hasMinSupport(0.02)
//                            && apriori.hasMinConfidence(0.8)) {
//                    if (value.getIssues().size() >= 2) {
                        projectVersionFilePairReleaseOcurrence.addVersionForFilePair(filePair, version, value.getIssues().size());
                    }
                }
//                 projectVersionFilePairReleaseOcurrence.getFilePairReleasesOccurenceCounter().get(new FilePair("camel-core/src/main/java/org/apache/camel/management/CamelNamingStrategy.java", "camel-core/src/main/java/org/apache/camel/impl/DefaultCamelContext.java"));
            }
            if (summaryRepositoryName.length() > 0) {
                summaryRepositoryName.append(", ");
            }
            summaryRepositoryName.append(project);

            releaseOccurrences.add(projectVersionFilePairReleaseOcurrence);
        }

        EntityMatrix matrixSummary = new EntityMatrix();
        matrixSummary.setNodes(objectsToNodes(releaseOccurrences, ProjectFilePairReleaseOcurrence.getHeader() + ";" + releaseOccurrences.iterator().next().getFilePairOcurrencesGroup().getDynamicHeader()));
        matrixSummary.setRepository(summaryRepositoryName.toString());
        matrixSummary.getParams().put("filename", "Projects summary");
        matrixSummary.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
        matricesToSave.add(matrixSummary);

    }

    // TODO parameterize
    private List<String> getSelectedProjects() {
        return Arrays.asList(new String[]{
            "camel",
            "cassandra",
            "cloudstack",
            "cxf",
            "derby",
            "hadoop",
            "hbase",
            "hive",
            "lucene",
            "solr",});
    }
}
