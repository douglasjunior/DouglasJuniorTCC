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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Report 1 - Projects Pair of File per Version
 *
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileQuantityInFixVersionAprioriServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileQuantityInFixVersionAprioriServices() {
        super(null, null);
    }

    public BichoPairOfFileQuantityInFixVersionAprioriServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileQuantityInFixVersionAprioriServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

        final int minOccurrencesInOneVersion = 2;
        if (getRepository() == null) {
            throw new IllegalArgumentException("Parameter repository must be informed.");
        }

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, getRepository(), getMaxFilesPerCommit());

        final Map<FilePair, Integer[]> pairFilesOccurrencesPerVersion = new HashMap<>();
        final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();
        final int totalFixVersion = fixVersionOrdered.size();

        for (String version : fixVersionOrdered) {

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

            for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet1 : pairFiles.entrySet()) {
                final FilePair key = entrySet1.getKey();
                final int occurrences = entrySet1.getValue().getIssuesId().size();
                final int indexOfVersion = fixVersionOrdered.indexOf(version);

                if (pairFilesOccurrencesPerVersion.containsKey(key)) {
                    final Integer[] quantity = pairFilesOccurrencesPerVersion.get(key);
                    if (quantity[indexOfVersion] == null) {
                        quantity[indexOfVersion] = occurrences;
                    } else {
                        quantity[indexOfVersion] += occurrences;
                    }
                } else {
                    final Integer[] quantity = new Integer[totalFixVersion];
                    quantity[indexOfVersion] = occurrences;
                    pairFilesOccurrencesPerVersion.put(key, quantity);
                }
            }

//            EntityMatrix matrix = new EntityMatrix();
//            matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeaderAprioriOnly()));
//            matrix.setNodes(objectsToNodes(pairFilesOccurrencesPerVersion, fixVersionOrdered));
//            matricesToSave.add(matrix);

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

        }

        Map<FilePair, Integer[]> filteredOccurrencesPerVersion = new HashMap<>();
        if (minOccurrencesInOneVersion > 1) {
            nextPairFile:
            for (Map.Entry<FilePair, Integer[]> entrySet : pairFilesOccurrencesPerVersion.entrySet()) {
                FilePair key = entrySet.getKey();
                Integer[] occurrencesPerVesion = entrySet.getValue();
                for (Integer occurrences : occurrencesPerVesion) {
                    if (occurrences != null && occurrences > minOccurrencesInOneVersion) {
                        filteredOccurrencesPerVersion.put(key, occurrencesPerVesion);
                        continue nextPairFile;
                    }
                }
            }
        } else {
            filteredOccurrencesPerVersion = pairFilesOccurrencesPerVersion;
        }

        EntityMatrix matrix = new EntityMatrix();
//        matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeaderAprioriOnly()));
        matrix.setNodes(objectsToNodes(filteredOccurrencesPerVersion, fixVersionOrdered));
        matricesToSave.add(matrix);
    }

    protected static List<EntityMatrixNode> objectsToNodes(final Map<FilePair, Integer[]> list, final List<String> versions) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        StringBuilder header = new StringBuilder("file1;file2");
        for (String version : versions) {
            header.append(";").append(version);
        }
        nodes.add(new EntityMatrixNode(header.toString()));

        for (Map.Entry<FilePair, Integer[]> entrySet : list.entrySet()) {
            FilePair filePair = entrySet.getKey();
            Integer[] value = entrySet.getValue();
            StringBuilder row = new StringBuilder(filePair.toString());
            for (Integer ocurrencesQuantity : value) {
                row.append(ocurrencesQuantity == null ? 0 : ocurrencesQuantity).append(";");
            }
            nodes.add(new EntityMatrixNode(row.toString()));
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
                if (apriori1.getHighestConfidence() > apriori2.getHighestConfidence()) {
                    return -1;
                } else if (apriori1.getHighestConfidence() < apriori2.getHighestConfidence()) {
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
