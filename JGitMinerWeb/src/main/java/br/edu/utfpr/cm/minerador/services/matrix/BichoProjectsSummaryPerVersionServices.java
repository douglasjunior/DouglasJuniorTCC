package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
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
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectVersion;
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectVersionSummary;
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
 * Report 2 - Projects Summary
 *
 * @author Rodrigo Kuroda
 */
public class BichoProjectsSummaryPerVersionServices extends AbstractBichoMatrixServices {

    public BichoProjectsSummaryPerVersionServices() {
        super(null, null);
    }

    public BichoProjectsSummaryPerVersionServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoProjectsSummaryPerVersionServices(GenericBichoDAO dao, GenericDao genericDao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, genericDao, repository, matricesToSave, params, out);
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
        return 2; //Util.stringToInteger(params.get("minOccurrencesInEachVersion") + "");
    }

    @Override
    public void run() {
        System.out.println(params);
        StringBuilder summaryRepositoryName = new StringBuilder();

        // TODO parametrizar
        // TODO confirmar/verificar de acordo com a planilha
        final Set<FilterByApriori> filters = FilterByApriori.getSuggestedFilters();
        final List<FilterFilePairByReleaseOcurrence> filtersOccurrences = FilterFilePairByReleaseOcurrence.getSuggestedFilters();

//        Double minSupport = getMinSupport();
//        Double maxSupport = getMaxSupport();
//        Double minConfidence = getMinConfidence();
//        Double maxConfidence = getMaxConfidence();
        int minFilePairOccurrences = 2;
        int minOccurrencesInVersion = getMinOccurencesInVersion();
//        boolean hasSupportFilter = minSupport != null && maxSupport != null;
//        boolean hasConfidenceFilter = minConfidence != null && maxConfidence != null;

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");
        final Set<ProjectVersionSummary> projectSummary = new LinkedHashSet<>();

        for (String project : getSelectedProjects()) {

            BichoDAO bichoDAO = new BichoDAO(dao, project, getMaxFilesPerCommit());
            BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, project, getMaxFilesPerCommit());
            BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, project, getMaxFilesPerCommit());

            final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();
            final List<Version> allVersions = VersionUtil.listStringToListVersion(fixVersionOrdered);
            final ProjectFilePairReleaseOcurrence projectVersionFilePairReleaseOcurrence
                    = new ProjectFilePairReleaseOcurrence(new Project(project), allVersions,
                            minFilePairOccurrences, minOccurrencesInVersion, filtersOccurrences,
                            FilterByApriori.getFiltersForExperiment1());

            final Set<ProjectVersionSummary> projectVersionsSummary = new LinkedHashSet<>(); // cummulative versions summary for project

            for (String versionString : fixVersionOrdered) {

                Version version = new Version(versionString);

                ProjectVersion projectVersion = new ProjectVersion(new Project(project), version);
                ProjectVersionSummary summaryVersion = new ProjectVersionSummary(projectVersion, filters);

                out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
                out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

                final Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
                identifyFilePairs(pairFiles, bichoDAO, versionString, bichoFileDAO);

                Set<Integer> allConsideredIssues = new HashSet<>();
                for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                    FilePairAprioriOutput value = entrySet.getValue();
                    allConsideredIssues.addAll(value.getIssuesId());
                }

                System.out.println("Calculing apriori...");
                Cacher cacher = new Cacher(bichoFileDAO, bichoPairFileDAO);
                for (FilePair fileFile : pairFiles.keySet()) {
                    Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version.getVersion());
                    Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version.getVersion());

                    FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

                    FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                            filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

                    fileFile.orderFilePairByConfidence(apriori);
                    filePairOutput.setFilePairApriori(apriori);

//                    if (apriori.hasMinMaxConfidence(minConfidence, maxConfidence)
                    //                            && apriori.hasMinMaxSupport(minSupport, maxSupport)) {
                    summaryVersion.addIssue(filePairOutput.getIssues());
                    summaryVersion.addCommit(filePairOutput.getCommits());
                    summaryVersion.addFilePair(fileFile);
                    summaryVersion.addFilePairApriori(apriori);
                    //                    }

                }

                projectSummary.add(summaryVersion);
                projectVersionsSummary.add(summaryVersion);

                for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                    FilePair filePair = entrySet.getKey();
                    FilePairAprioriOutput value = entrySet.getValue();
                    projectVersionFilePairReleaseOcurrence.addVersionForFilePair(filePair, version, value.getIssues().size());
                }

                for (FilePair filePair : summaryVersion.getFilePairs()) {
                    if (projectVersionFilePairReleaseOcurrence.hasMinimumOccurrences(filePair)) {
                        summaryVersion.addFilePairWithAtLeastTwoOccurrencesInAnyVersion(filePair);
                    }
                }
            }
            if (summaryRepositoryName.length() > 0) {
                summaryRepositoryName.append(", ");
            }
            summaryRepositoryName.append(project);
        }

        EntityMatrix matrixSummary = new EntityMatrix();
        matrixSummary.setNodes(objectsToNodes(projectSummary, ProjectVersionSummary.getHeader() + ";" + projectSummary.iterator().next().getFilePairsAprioriStatistics().getDynamicHeader()));
        matrixSummary.setRepository(summaryRepositoryName.toString());
        matrixSummary.getParams().put("filename", "summary");
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
