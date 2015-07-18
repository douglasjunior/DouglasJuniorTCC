package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilterFilePairByReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.matrix.model.Project;
import br.edu.utfpr.cm.minerador.services.matrix.model.ProjectFilePairReleaseOcurrence;
import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import br.edu.utfpr.cm.minerador.services.util.VersionUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private int getMinOccurencesInAnyVersion() {
        return 2; //Util.stringToInteger(params.get("minOccurrencesInEachVersion") + "");
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
        int minOccurrencesInAnyVersion = getMinOccurencesInAnyVersion();
        boolean hasSupportFilter = minSupport != null && maxSupport != null;
        boolean hasConfidenceFilter = minConfidence != null && maxConfidence != null;

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");
        final Set<ProjectFilePairReleaseOcurrence> releaseOccurrences = new LinkedHashSet<>();

        for (String project : getSelectedProjects()) {

            BichoDAO bichoDAO = new BichoDAO(dao, project, getMaxFilesPerCommit());
            BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, project, getMaxFilesPerCommit());

            final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();

            final ProjectFilePairReleaseOcurrence projectVersionFilePairReleaseOcurrence
                    = new ProjectFilePairReleaseOcurrence(new Project(project),
                            VersionUtil.listStringToListVersion(fixVersionOrdered),
                            minOccurrencesInAnyVersion, filters);

            for (String versionString : fixVersionOrdered) {
                Version version = new Version(versionString);
                Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();

                out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
                out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

                Set<FilePath> allFiles = new HashSet<>();
                Set<FilePath> allTestJavaFiles = new HashSet<>();
                Set<FilePath> allJavaFiles = new HashSet<>();
                Set<FilePath> allXmlFiles = new HashSet<>();
                Set<FilePath> allFilteredFiles = new HashSet<>();

                Set<Commit> allCommits = new HashSet<>();
                Set<Integer> allConsideredCommits = new HashSet<>();
                Set<Integer> allDefectIssues = new HashSet<>();

                // select a issue/pullrequest commenters
                Map<Issue, List<Commit>> issuesConsideredCommits = bichoDAO.selectIssuesAndType(versionString);

                if (issuesConsideredCommits.isEmpty()) {
                    continue;
                }

                Set<Issue> allIssues = issuesConsideredCommits.keySet();

                out.printLog("Issues (filtered): " + issuesConsideredCommits.size());

                int count = 1;

                // combina em pares todos os arquivos commitados em uma issue
                final int totalIssues = issuesConsideredCommits.size();
                int progressFilePairing = 0;
                for (Map.Entry<Issue, List<Commit>> entrySet : issuesConsideredCommits.entrySet()) {
                    if (++progressFilePairing % 100 == 0
                            || progressFilePairing == totalIssues) {
                        System.out.println(progressFilePairing + "/" + totalIssues);
                    }
                    Issue issue = entrySet.getKey();
                    List<Commit> commits = entrySet.getValue();

                    out.printLog("Issue #" + issue);
                    out.printLog(count++ + " of the " + issuesConsideredCommits.size());

                    out.printLog(commits.size() + " commits references the issue");
                    allCommits.addAll(commits);

                    List<FilePath> commitedFiles
                            = filterAndAggregateAllFileOfIssue(commits, bichoFileDAO, allFiles, allTestJavaFiles, allFilteredFiles, allJavaFiles, allXmlFiles);

                    // empty
                    if (commitedFiles.isEmpty()) {
                        out.printLog("No file commited for issue #" + issue);
                        continue;
                    } else if (commitedFiles.size() == 1) {
                        out.printLog("One file only commited for issue #" + issue);
                        continue;

                    }
                    out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

                    pairFiles(commitedFiles, pairFiles, issue, allDefectIssues, allConsideredCommits);
                }

                if (pairFiles.isEmpty()) {
                    continue;
                }

                projectVersionFilePairReleaseOcurrence.addFilePair(pairFiles.keySet());
                projectVersionFilePairReleaseOcurrence.addVersionForFilePair(pairFiles.keySet(), version);
                projectVersionFilePairReleaseOcurrence.addVersion(version);
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
        matrixSummary.getParams().put("filename", "summary");
        matricesToSave.add(matrixSummary);

    }

    private List<FilePath> filterAndAggregateAllFileOfIssue(List<Commit> commits, BichoFileDAO bichoFileDAO, Set<FilePath> allFiles, Set<FilePath> allTestJavaFiles, Set<FilePath> allFilteredFiles, Set<FilePath> allJavaFiles, Set<FilePath> allXmlFiles) {
        // monta os pares com os arquivos de todos os commits da issue
        List<FilePath> commitedFiles = new ArrayList<>();
        for (Commit commit : commits) {

            // select name of commited files
            List<FilePath> files = bichoFileDAO.selectFilesByCommitId(commit.getId());

            allFiles.addAll(files);

            out.printLog(files.size() + " files in commit #" + commit.getId());
            for (FilePath file : files) {
                if (file.getFilePath().endsWith("Test.java")
                        || file.getFilePath().toLowerCase().endsWith("_test.java")) {
                    allTestJavaFiles.add(file);
                    allFilteredFiles.add(file);
                } else if (!file.getFilePath().endsWith(".java")
                        && !file.getFilePath().endsWith(".xml")) {
                    allFilteredFiles.add(file);
                } else {
                    if (file.getFilePath().endsWith(".java")) {
                        allJavaFiles.add(file);
                    } else if (file.getFilePath().endsWith(".xml")) {
                        allXmlFiles.add(file);
                    }
                    commitedFiles.add(file);
                }
            }
        }
        return commitedFiles;
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
