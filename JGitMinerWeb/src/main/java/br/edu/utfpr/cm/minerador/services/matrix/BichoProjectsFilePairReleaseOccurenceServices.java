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
import br.edu.utfpr.cm.minerador.services.matrix.model.FilterByApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilterFilePairByReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Project;
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectFilePairReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.util.VersionUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

        int minFilePairOccurrences = 1; //TODO parametrizar
        int minOccurrencesInVersion = getMinOccurencesInVersion();

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");

        final Set<FilterByApriori> filtersForExperiment1 = FilterByApriori.getFiltersForExperiment1();
        final Map<FilterByApriori, Set<ProjectFilePairReleaseOcurrence>> releaseOccurrencesMap = new LinkedHashMap<>();
        final Set<ProjectFilePairReleaseOcurrence> summaries = new LinkedHashSet<>();

        for (FilterByApriori aprioriFilter : filtersForExperiment1) {
            releaseOccurrencesMap.put(aprioriFilter, new HashSet<>(getSelectedProjects().size()));
        }

        for (String project : getSelectedProjects()) {

            BichoDAO bichoDAO = new BichoDAO(dao, project, getMaxFilesPerCommit());
            BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, project, getMaxFilesPerCommit());
            BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, project, getMaxFilesPerCommit());

            final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();

            final ProjectFilePairReleaseOcurrence summary = new ProjectFilePairReleaseOcurrence(new Project(project),
                    VersionUtil.listStringToListVersion(fixVersionOrdered),
                    minFilePairOccurrences, minOccurrencesInVersion, filters, filtersForExperiment1);
            summaries.add(summary);

            final Map<FilterByApriori, ProjectFilePairReleaseOcurrence> projectVersionFilePairReleaseOcurrence
                    = new LinkedHashMap<>();
            for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                final ProjectFilePairReleaseOcurrence projectFilePairReleaseOcurrence = new ProjectFilePairReleaseOcurrence(new Project(project),
                        VersionUtil.listStringToListVersion(fixVersionOrdered),
                        minFilePairOccurrences, minOccurrencesInVersion, filters, filtersForExperiment1);
                projectVersionFilePairReleaseOcurrence.put(aprioriFilter, projectFilePairReleaseOcurrence);
                releaseOccurrencesMap.get(aprioriFilter).add(projectFilePairReleaseOcurrence);
            }

            for (String versionString : fixVersionOrdered) {
                Version version = new Version(versionString);

                out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
                out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

                final Map<FilterByApriori, Set<FilePairAprioriOutput>> output = new HashMap<>();
                for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                    output.put(aprioriFilter, new HashSet<>());
                }

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

                    summary.addVersionForFilePair(filePair, version, value.getIssues().size());
                    for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                        if (apriori.fits(aprioriFilter)) {
                            output.get(aprioriFilter).add(value);
                            projectVersionFilePairReleaseOcurrence.get(aprioriFilter).addVersionForFilePair(filePair, version, value.getIssues().size());
                            projectVersionFilePairReleaseOcurrence.get(aprioriFilter).addPairFileForAprioriFilter(filePair, aprioriFilter);
                            summary.addPairFileForAprioriFilter(filePair, aprioriFilter);
                        }
                    }
//                    if (apriori.hasMinIssues(3)
//                            && apriori.hasMinConfidence(0.9)) {
//                    if (apriori.hasMinSupport(0.02)
//                            && apriori.hasMinConfidence(0.8)) {
//                    if (value.getIssues().size() >= 2) {
//                    }
                }

                for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                    EntityMatrix matrix = new EntityMatrix();
                    final List<FilePairAprioriOutput> matrixNodes = new ArrayList<>(output.get(aprioriFilter));
                    orderByFilePairConfidenceAndSupport(matrixNodes);

                    matrix.setNodes(objectsToNodes(matrixNodes, FilePairAprioriOutput.getToStringHeader()));
                    matrix.setRepository(project);
                    matrix.getParams().put("filename", version.getVersion());
                    matrix.getParams().put("additionalFilename", aprioriFilter.toString());
                    matrix.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
                    matrix.getParams().put("aprioriFilter", aprioriFilter.toString());
                    matrix.getParams().put("version", version.getVersion());
                    matrix.getParams().put("project", project);
                    matricesToSave.add(matrix);
                }
            }
            if (summaryRepositoryName.length() > 0) {
                summaryRepositoryName.append(", ");
            }
            summaryRepositoryName.append(project);

        }

        releaseOccurrencesMap.entrySet().stream().map((entrySet) -> {
            FilterByApriori aprioriFilter = entrySet.getKey();
            Set<ProjectFilePairReleaseOcurrence> releaseOccurrences = entrySet.getValue();
            EntityMatrix matrixSummary = new EntityMatrix();
            matrixSummary.setNodes(objectsToNodes(releaseOccurrences, ProjectFilePairReleaseOcurrence.getHeader() + ";" + releaseOccurrences.iterator().next().getDynamicHeader()));
            matrixSummary.setRepository(summaryRepositoryName.toString());
            matrixSummary.getParams().put("filename", "summary " + aprioriFilter.toString());
            matrixSummary.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
            matrixSummary.getParams().put("aprioriFilter", aprioriFilter.toString());
            return matrixSummary;
        }).forEachOrdered((matrixSummary) -> {
            matricesToSave.add(matrixSummary);
        });

        EntityMatrix matrixSummary = new EntityMatrix();
        matrixSummary.setNodes(objectsToNodes(summaries, ProjectFilePairReleaseOcurrence.getHeader() + ";" + summaries.iterator().next().getDynamicHeader()));
        matrixSummary.setRepository(summaryRepositoryName.toString());
        matrixSummary.getParams().put("filename", "summary");
        matrixSummary.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);

        matricesToSave.add(matrixSummary);
    }

    // TODO parameterize
    private List<String> getSelectedProjects() {
        return Arrays.asList(new String[]{
            //              "camel"//,
//            "cassandra"//,
//            "cloudstack"//,
//            "cxf"//,
//            "derby"//,
//            "hadoop"//,
            //                    "hbase",
//            "hive"//,
//            "lucene"//,
                    "solr"
        });
    }
}
