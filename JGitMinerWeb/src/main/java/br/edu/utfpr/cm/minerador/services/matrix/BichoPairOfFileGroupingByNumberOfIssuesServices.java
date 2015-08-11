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
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.matrix.model.Project;
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectFilePairReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileGroupingByNumberOfIssuesServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileGroupingByNumberOfIssuesServices() {
        super(null, null);
    }

    public BichoPairOfFileGroupingByNumberOfIssuesServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileGroupingByNumberOfIssuesServices(GenericBichoDAO dao, GenericDao genericDao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, genericDao, repository, matricesToSave, params, out);
    }

    private Integer getMaxFilesPerCommit() {
        return Util.stringToInteger(params.get("maxFilesPerCommit") + "");
    }

    private Integer getMinFilesPerCommit() {
        return Util.stringToInteger(params.get("minFilesPerCommit") + "");
    }

    private boolean isOnlyFixed() {
        return "true".equalsIgnoreCase(params.get("mergedOnly") + "");
    }

    public List<String> getFilesToIgnore() {
        return getStringLinesParam("filesToIgnore", true, false);
    }

    public List<String> getFilesToConsiders() {
        return getStringLinesParam("filesToConsiders", true, false);
    }

    public Integer getQuantity() {
        return getIntegerParam("quantity");
    }

    public Integer getGroupsQuantity() {
        return getIntegerParam("groupsQuantity");
    }

    public Integer getFutureQuantity() {
        return getIntegerParam("futureQuantity");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parameter repository must be informed.");
        }

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, getRepository(), getMaxFilesPerCommit());

        out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
        out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

        final long quantity;
        if (getGroupsQuantity() != null && getGroupsQuantity() > 0) {
            quantity = Double.valueOf(Math.ceil(bichoDAO.calculeNumberOfIssues() / getGroupsQuantity().doubleValue())).intValue();

        } else if (getQuantity() != null && getQuantity() > 0) {
            quantity = getQuantity();

        } else {
            throw new IllegalArgumentException("Parameter quantity or group quantity is required.");
        }

        // select a issue/pullrequest commenters
        final List<Map<Issue, List<Commit>>> subdividedIssuesCommits = bichoDAO.selectAllIssuesAndTypeSubdividedBy(quantity);

        out.printLog("Issues (filtered): " + subdividedIssuesCommits.size());

        final Cacher cacher = new Cacher(bichoFileDAO);

        // Mapping indexes with encapsulated indexes for perfomance
        final Map<Integer, Version> indexesMap = new HashMap<>(subdividedIssuesCommits.size());
        // Using version to encapsulate version and reuse the functionalities provided for version
        final List<Version> indexes = new ArrayList<>(subdividedIssuesCommits.size());
        for (int i = 0; i < subdividedIssuesCommits.size() - 1; i++) {
            final Version version = new Version(String.valueOf(i));
            indexes.add(version);
            indexesMap.put(i, version);
        }

        final Set<FilterByApriori> filtersForExperiment1 = FilterByApriori.getFiltersForExperiment1();
        final Map<FilterByApriori, Set<ProjectFilePairReleaseOcurrence>> releaseOccurrencesMap = new LinkedHashMap<>();
        final Set<ProjectFilePairReleaseOcurrence> summaries = new LinkedHashSet<>();

        for (FilterByApriori aprioriFilter : filtersForExperiment1) {
            releaseOccurrencesMap.put(aprioriFilter, new HashSet<>());

        }

        // TODO parametrizar
        // TODO confirmar/verificar de acordo com a planilha
        final List<FilterFilePairByReleaseOcurrence> filters = FilterFilePairByReleaseOcurrence.getSuggestedFilters();
        final Project project = new Project(getRepository());
        final int minFilePairOccurrences = 1;
        final int minOccurrencesInVersion = 1;

        final ProjectFilePairReleaseOcurrence summary = new ProjectFilePairReleaseOcurrence(project,
                indexes,
                minFilePairOccurrences, minOccurrencesInVersion, filters, filtersForExperiment1);
        summaries.add(summary);

        final Map<FilterByApriori, ProjectFilePairReleaseOcurrence> projectVersionFilePairReleaseOcurrence
                = new LinkedHashMap<>();
        for (FilterByApriori aprioriFilter : filtersForExperiment1) {
            final ProjectFilePairReleaseOcurrence projectFilePairReleaseOcurrence = new ProjectFilePairReleaseOcurrence(project,
                    indexes,
                    minFilePairOccurrences, minOccurrencesInVersion, filters, filtersForExperiment1);
            projectVersionFilePairReleaseOcurrence.put(aprioriFilter, projectFilePairReleaseOcurrence);
            releaseOccurrencesMap.get(aprioriFilter).add(projectFilePairReleaseOcurrence);
        }

        // combina em pares todos os arquivos commitados em uma issue
        for (int index = 0; index < subdividedIssuesCommits.size() - 1; index++) {
            final Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
            final Map<Issue, List<Commit>> issuesCommits = subdividedIssuesCommits.get(index);
            final Set<Issue> issues = issuesCommits.keySet();
            final Set<Issue> futureIssues = subdividedIssuesCommits.get(index + 1).keySet();

            identifyFilePairs(pairFiles, issuesCommits, bichoFileDAO);

            out.printLog("Result: " + pairFiles.size());

            out.printLog("Index: " + index + "/" + subdividedIssuesCommits.size());
            out.printLog("Counting future defects...");
            final int total = pairFiles.keySet().size();
            int progressCountFutureDefects = 0;
            for (FilePair fileFile : pairFiles.keySet()) {
                if (++progressCountFutureDefects % 100 == 0
                        || progressCountFutureDefects == total) {
                    System.out.println(progressCountFutureDefects + "/" + total);
                }
                Map<String, Set<Integer>> futureFilePairIssues = bichoPairFileDAO.selectIssues(
                        fileFile.getFile1(), fileFile.getFile2(),
                        futureIssues);

                if (futureFilePairIssues.get("Bug") != null) {
                    pairFiles.get(fileFile).addFutureDefectIssuesId(futureFilePairIssues.get("Bug"));
                }
                Set<Integer> allIssuesId = new HashSet<>();
                for (Map.Entry<String, Set<Integer>> entrySet : futureFilePairIssues.entrySet()) {
                    Set<Integer> value = entrySet.getValue();
                    allIssuesId.addAll(value);
                }
                pairFiles.get(fileFile).addFutureIssuesId(allIssuesId);
            }

            Set<Integer> allConsideredIssues = new HashSet<>();
            for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                FilePairAprioriOutput value = entrySet.getValue();
                allConsideredIssues.addAll(value.getIssuesId());
            }
            // calculando o apriori
            out.printLog("Calculing apriori...");
            out.printLog("Issues index " + index + " (x 100): " + allConsideredIssues.size());
            int totalApriori = pairFiles.size();
            int countApriori = 0;

            final List<FilePairAprioriOutput> pairFileList = new ArrayList<>();

            for (FilePair fileFile : pairFiles.keySet()) {
                if (++countApriori % 100 == 0
                        || countApriori == totalApriori) {
                    System.out.println(countApriori + "/" + totalApriori);
                }

                Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), issues);
                Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), issues);

                FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

                FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                        filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

                fileFile.orderFilePairByConfidence(apriori);
                filePairOutput.setFilePairApriori(apriori);

                pairFileList.add(filePairOutput);
            }

            final Map<FilterByApriori, Set<FilePairAprioriOutput>> output = new HashMap<>();
            for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                output.put(aprioriFilter, new HashSet<>());
            }

            for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
                FilePair filePair = entrySet.getKey();
                FilePairAprioriOutput value = entrySet.getValue();
                final FilePairApriori apriori = value.getFilePairApriori();

                summary.addVersionForFilePair(filePair, indexesMap.get(index), value.getIssues().size());
                for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                    if (apriori.fits(aprioriFilter)) {
                        output.get(aprioriFilter).add(value);
                        projectVersionFilePairReleaseOcurrence.get(aprioriFilter).addVersionForFilePair(filePair, indexesMap.get(index), value.getIssues().size());
                        projectVersionFilePairReleaseOcurrence.get(aprioriFilter).addPairFileForAprioriFilter(filePair, aprioriFilter);
                        summary.addPairFileForAprioriFilter(filePair, aprioriFilter);
                    }
                }

            }
            for (FilterByApriori aprioriFilter : filtersForExperiment1) {
                EntityMatrix matrix = new EntityMatrix();
                final List<FilePairAprioriOutput> matrixNodes = new ArrayList<>(output.get(aprioriFilter));
                orderByFilePairConfidenceAndSupport(matrixNodes);

                matrix.setNodes(objectsToNodes(matrixNodes, FilePairAprioriOutput.getToStringHeader()));
                matrix.setRepository(project.getName());
                matrix.getParams().put("filename", indexesMap.get(index).getVersion());
                matrix.getParams().put("additionalFilename", aprioriFilter.toString());
                matrix.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
                matrix.getParams().put("aprioriFilter", aprioriFilter.toString());
                matrix.getParams().put("index", indexesMap.get(index).getVersion());
                matrix.getParams().put("project", project.getName());
                
                saveMatrix(matrix, getClass());
            }
        }

        releaseOccurrencesMap.entrySet().stream().map((entrySet) -> {
            FilterByApriori aprioriFilter = entrySet.getKey();
            Set<ProjectFilePairReleaseOcurrence> releaseOccurrences = entrySet.getValue();
            EntityMatrix matrixSummary = new EntityMatrix();
            matrixSummary.setNodes(objectsToNodes(releaseOccurrences, ProjectFilePairReleaseOcurrence.getHeader() + ";" + releaseOccurrences.iterator().next().getDynamicHeader()));
            matrixSummary.setRepository(project.getName());
            matrixSummary.getParams().put("filename", "summary " + aprioriFilter.toString());
            matrixSummary.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
            matrixSummary.getParams().put("aprioriFilter", aprioriFilter.toString());
            return matrixSummary;
        }).forEachOrdered((matrixSummary) -> {
            
            saveMatrix(matrixSummary, getClass());
        });

        EntityMatrix matrixSummary = new EntityMatrix();
        matrixSummary.setNodes(objectsToNodes(summaries, ProjectFilePairReleaseOcurrence.getHeader() + ";" + summaries.iterator().next().getDynamicHeader()));
        matrixSummary.setRepository(project.getName());
        matrixSummary.getParams().put("filename", "summary");
        matrixSummary.getParams().put("minOccurrencesInVersion", minOccurrencesInVersion);
        
        saveMatrix(matrixSummary, getClass());
    }
}
