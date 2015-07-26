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
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class BichoPairOfFileGroupingByNumberOfIssuesServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileGroupingByNumberOfIssuesServices() {
        super(null, null);
    }

    public BichoPairOfFileGroupingByNumberOfIssuesServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileGroupingByNumberOfIssuesServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

    public Integer getQuantity() {
        return getIntegerParam("quantity");
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

        Integer quantity = getQuantity();

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

        // select a issue/pullrequest commenters
        List<Map<Issue, List<Commit>>> subdividedIssuesCommits = bichoDAO.selectAllIssuesAndTypeSubdividedBy(quantity);

        out.printLog("Issues (filtered): " + subdividedIssuesCommits.size());

        Cacher cacher = new Cacher(bichoFileDAO);

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
            orderByFilePairSupportAndNumberOfDefects(pairFileList);

            EntityMatrix matrix = new EntityMatrix();
            matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeader()));
            matrix.setAdditionalFilename(String.valueOf(index + 1));
            matrix.getParams().put("index", index);
            matricesToSave.add(matrix);

            saveTop25Matrix(pairFileList, index);
        }
    }

    private void saveTop25Matrix(List<FilePairAprioriOutput> pairFileList, int index) {
        // 25 arquivos distintos com maior confianÃƒÂ§a entre o par (coluna da esquerda)
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

        log("\n\n" + getRepository() + " " + index + " top 25 \n"
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
        top25.setNodes(objectsToNodes(nodesTop25, FilePairAprioriOutput.getToStringHeader()));
        top25.setAdditionalFilename("top 25");
        matricesToSave.add(top25);
    }

    private void orderByFilePairSupportAndNumberOfDefects(final List<FilePairAprioriOutput> pairFileList) {
        // order by number of defects (lower priority)
        orderByNumberOfDefects(pairFileList);
        // order by support (higher priority)
        orderByFilePairSupport(pairFileList);
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
