package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileInFixVersionAprioriServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileInFixVersionAprioriServices() {
        super(null, null);
    }

    public BichoPairOfFileInFixVersionAprioriServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileInFixVersionAprioriServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
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

    public String getVersion() {
        return getStringParam("version");
    }

    public String getFutureVersion() {
        return getStringParam("futureVersion");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parameter repository must be informed.");
        }

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");

        String version = getVersion();
        String futureVersion = getFutureVersion();

        Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
//        Set<FilePath> allDistinctFiles = new HashSet<>();

//        Pattern fileToConsiders = null;
//        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
//            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
//        }

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, getRepository(), getMaxFilesPerCommit());

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
        Map<Issue, List<Commit>> issuesConsideredCommits = bichoDAO.selectIssuesAndType(version);
        Set<Issue> allIssues = issuesConsideredCommits.keySet();
        
        out.printLog("Issues (filtered): " + issuesConsideredCommits.size());

        int count = 1;
        Cacher cacher = new Cacher(bichoFileDAO);

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
//            allDistinctFiles.addAll(commitedFiles);

            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

            pairFiles(commitedFiles, pairFiles, issue, allDefectIssues, allConsideredCommits);

        }

        out.printLog("Result: " + pairFiles.size());

        Set<Integer> allConsideredIssues = new HashSet<>();
        for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
            FilePairAprioriOutput value = entrySet.getValue();
            allConsideredIssues.addAll(value.getIssuesId());
        }
        // calculando o apriori
        out.printLog("Calculing apriori...");
        out.printLog("Issues considered in version " + version + ": " + allConsideredIssues.size());
        int totalApriori = pairFiles.size();
        int countApriori = 0;

        final List<FilePairAprioriOutput> pairFileList = new ArrayList<>();

        for (FilePair fileFile : pairFiles.keySet()) {
            if (++countApriori % 100 == 0
                    || countApriori == totalApriori) {
                System.out.println(countApriori + "/" + totalApriori);
            }

            Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version);
            Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version);

            FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

            FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                    filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

            fileFile.orderFilePairByConfidence(apriori);
            filePairOutput.setFilePairApriori(apriori);

            pairFileList.add(filePairOutput);
        }
        orderByFilePairSupportAndConfidence(pairFileList);

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeader()));
        matricesToSave.add(matrix);

        out.printLog("\n\n" + getRepository() + " " + version + "\n"
                + "Number of files (JAVA and XML): " + allFiles.size() + "\n"
                + "Number of files (JAVA): " + allJavaFiles.size() + "\n"
                + "Number of files (XML): " + allXmlFiles.size() + "\n"
                + "Number of ignored files !.java, !.xml, *Test.java: " + allFilteredFiles.size() + "\n"
                + "Number of ignored files *Test.java: " + allTestJavaFiles.size() + "\n"
                + "Number of file pairs: " + pairFileList.size() + "\n"
                + "Number of issues: " + allIssues.size() + "\n"
                + "Number of considered issues: " + allConsideredIssues.size() + "\n"
                + "Number of commits: " + allCommits.size() + "\n"
                + "Number of considered commits: " + allConsideredCommits.size() + "\n"
                + "Number of defect issues: " + allDefectIssues.size() + "\n"
        );

        log("\n\n" + getRepository() + " " + version + "\n"
                + "Number of files (JAVA and XML): " + allFiles.size() + "\n"
                + "Number of files (JAVA): " + allJavaFiles.size() + "\n"
                + "Number of files (XML): " + allXmlFiles.size() + "\n"
                + "Number of ignored files !.java, !.xml, *Test.java: " + allFilteredFiles.size() + "\n"
                + "Number of ignored files *Test.java: " + allTestJavaFiles.size() + "\n"
                + "Number of file pairs: " + pairFileList.size() + "\n"
                + "Number of issues: " + allIssues.size() + "\n"
                + "Number of considered issues: " + allConsideredIssues.size() + "\n"
                + "Number of commits: " + allCommits.size() + "\n"
                + "Number of considered commits: " + allConsideredCommits.size() + "\n"
                + "Number of defect issues: " + allDefectIssues.size() + "\n"
        );

//        saveTop25Matrix(pairFileList);
    }

    protected static List<EntityMatrixNode> objectsToNodes(List<FilePairAprioriOutput> list, String header) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        nodes.add(new EntityMatrixNode(header));
        for (FilePairAprioriOutput value : list) {
            nodes.add(new EntityMatrixNode(value.toString()));
        }
        return nodes;
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

    private void saveTop25Matrix(List<FilePairAprioriOutput> pairFileList) {
        // 25 arquivos distintos com maior confianÃ§a entre o par (coluna da esquerda)
        final Set<FilePairAprioriOutput> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>(25);
        final List<FilePairAprioriOutput> nodesTop25 = new ArrayList<>();
        for (FilePairAprioriOutput node : pairFileList) {
            distinctFileOfFilePairWithHigherConfidence.add(node);
            nodesTop25.add(node);
            if (distinctFileOfFilePairWithHigherConfidence.size() >= 25) {
                break;
            }
        }

        Set<Integer> totalIssues = new HashSet<>();
        Set<Integer> totalCommits = new HashSet<>();
        Set<Integer> totalCommitsFile1 = new HashSet<>();
        Set<Integer> totalCommitsFile2 = new HashSet<>();
        Set<Integer> totalDefects = new HashSet<>();
        Set<Integer> totalFutureDefects = new HashSet<>();

        Set<String> allFiles = new HashSet<>();
        Set<String> allJavaFiles = new HashSet<>();
        Set<String> allXmlFiles = new HashSet<>();
        Set<String> allOtherFiles = new HashSet<>();

        for (FilePairAprioriOutput node : nodesTop25) {
            totalIssues.addAll(node.getIssuesId());
            totalCommits.addAll(node.getCommitsId());

            totalCommitsFile1.addAll(node.getCommitsFile1Id());
            totalCommitsFile2.addAll(node.getCommitsFile2Id());

            totalFutureDefects.addAll(node.getFutureDefectIssuesId());
            totalDefects.add(node.getCommitsIdWeight());

            final String file1 = node.getFilePair().getFile1();

            allFiles.add(file1);
            if (file1.endsWith(".java")) {
                allJavaFiles.add(file1);
            } else if (file1.endsWith(".xml")) {
                allXmlFiles.add(file1);
            } else {
                allOtherFiles.add(file1);
            }

            final String file2 = node.getFilePair().getFile2();

            allFiles.add(file2);
            if (file2.endsWith(".java")) {
                allJavaFiles.add(file2);
            } else if (file2.endsWith(".xml")) {
                allXmlFiles.add(file2);
            } else {
                allOtherFiles.add(file2);
            }
        }

        FilePairAprioriOutput summary = new FilePairAprioriOutput(new FilePair("Summary", String.valueOf(allFiles.size())));
        summary.addIssueId(totalIssues.size());
        summary.addCommitId(totalCommits.size());
        summary.addCommitFile1Id(totalCommitsFile1.size());
        summary.addCommitFile2Id(totalCommitsFile2.size());
        summary.addDefectIssueId(totalDefects.size());
        summary.addFutureDefectIssuesId(totalFutureDefects.size());

        log("\n\n" + getRepository() + " " + getVersion() + " top 25 \n"
                + "Number of files: " + allFiles.size() + "\n"
                + "Number of files (JAVA): " + allJavaFiles.size() + "\n"
                + "Number of files (XML): " + allXmlFiles.size() + "\n"
                + "Number of files (Others): " + allOtherFiles.size() + "\n"
                + "Number of commits file 1: " + totalCommitsFile1.size() + "\n"
                + "Number of commits file 2: " + totalCommitsFile2.size() + "\n"
                + "Number of issues: " + totalIssues.size() + "\n"
                + "Number of commits: " + totalCommits.size() + "\n"
                + "Number of defect issues: " + totalDefects.size() + "\n"
                + "Number of future defect issues: " + totalFutureDefects.size() + "\n\n"
        );

        EntityMatrix top25 = new EntityMatrix();
        top25.setNodes(objectsToNodes(nodesTop25, FilePairAprioriOutput.getToStringHeaderAprioriOnly()));
        top25.setAdditionalFilename("top 25");
        matricesToSave.add(top25);
    }

    private void orderByFilePairSupportAndConfidence(final List<FilePairAprioriOutput> pairFileList) {
        orderByFilePairSupport(pairFileList);
        orderByFilePairConfidence(pairFileList);
    }

    private void orderByFilePairSupport(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new Comparator<FilePairAprioriOutput>() {

            @Override
            public int compare(FilePairAprioriOutput o1, FilePairAprioriOutput o2) {
                FilePairApriori apriori1 = o1.getFilePairApriori();
                FilePairApriori apriori2 = o2.getFilePairApriori();
                if (apriori1.getSupportFilePair() > apriori2.getSupportFilePair()) {
                    return -1;
                } else if (apriori1.getSupportFilePair() < apriori2.getSupportFilePair()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void orderByFilePairConfidence(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new Comparator<FilePairAprioriOutput>() {

            @Override
            public int compare(FilePairAprioriOutput o1, FilePairAprioriOutput o2) {
                FilePairApriori apriori1 = o1.getFilePairApriori();
                FilePairApriori apriori2 = o2.getFilePairApriori();
                if (apriori1.getHigherConfidence() > apriori2.getHigherConfidence()) {
                    return -1;
                } else if (apriori1.getHigherConfidence() < apriori2.getHigherConfidence()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void orderByNumberOfDefects(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new Comparator<FilePairAprioriOutput>() {

            @Override
            public int compare(FilePairAprioriOutput o1, FilePairAprioriOutput o2) {
                final int defectIssuesIdWeight1 = o1.getFutureDefectIssuesIdWeight();
                final int defectIssuesIdWeight2 = o2.getFutureDefectIssuesIdWeight();
                if (defectIssuesIdWeight1 > defectIssuesIdWeight2) {
                    return -1;
                } else if (defectIssuesIdWeight1 < defectIssuesIdWeight2) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
