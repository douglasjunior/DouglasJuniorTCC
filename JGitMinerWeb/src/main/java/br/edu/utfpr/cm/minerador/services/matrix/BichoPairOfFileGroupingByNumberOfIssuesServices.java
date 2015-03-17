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
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

        Map<FilePair, FilePairOutput> pairFiles = new HashMap<>();
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
        List<Map<Issue, List<Integer>>> subdividedIssuesCommits = bichoDAO.selectAllIssuesAndTypeSubdividedBy(quantity);

        out.printLog("Issues (filtered): " + subdividedIssuesCommits.size());

        int count = 1;
        int numberFilePairs = 0;
        Cacher cacher = new Cacher(bichoFileDAO);

        // combina em pares todos os arquivos commitados em uma issue
        int progressFilePairing = 0;
        for (int index = 0; index < subdividedIssuesCommits.size() - 1; index++) {
            final Map<Issue, List<Integer>> issuesCommits = subdividedIssuesCommits.get(index);
            final Set<Issue> issues = issuesCommits.keySet();
            final Set<Issue> futureIssues = subdividedIssuesCommits.get(index + 1).keySet();
            final int totalIssues = issues.size();
            for (Map.Entry<Issue, List<Integer>> entrySet : issuesCommits.entrySet()) {
                if (++progressFilePairing % 10 == 0
                        || progressFilePairing == totalIssues) {
                    System.out.println(progressFilePairing + "/" + totalIssues);
                }
                Issue issue = entrySet.getKey();
                List<Integer> commits = entrySet.getValue();

                out.printLog("Issue #" + issue);
                out.printLog(count++ + " of the " + subdividedIssuesCommits.size());

                out.printLog(commits.size() + " commits references the issue");

                // monta os pares com os arquivos de todos os commits da issue
                List<FilePath> commitedFiles = new ArrayList<>();
                for (Integer commit : commits) {

                    // select name of commited files
                    List<FilePath> files = bichoFileDAO.selectFilesByCommitId(commit);
                    out.printLog(files.size() + " files in commit #" + commit);
                    commitedFiles.addAll(files);
                }

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

                int numberPairFilesInIssue = 0;
                for (int i = 0; i < commitedFiles.size(); i++) {
                    FilePath file1 = commitedFiles.get(i);
                    for (int j = i + 1; j < commitedFiles.size(); j++) {
                        FilePath file2 = commitedFiles.get(j);
                        if (!file1.getFilePath().equals(file2.getFilePath())) {
                            FilePair filePair = new FilePair(file1.getFilePath(), file2.getFilePath());
                            if (pairFiles.containsKey(filePair)) {
                                FilePairOutput filePairOutput = pairFiles.get(filePair);
                                filePairOutput.addIssueId(issue.getId());
                                if (file1.getCommitId().equals(file2.getCommitId())) {
                                    filePairOutput.addCommitId(file1.getCommitId());
                                }
                                filePairOutput.addCommitFile1Id(file1.getCommitId());
                                filePairOutput.addCommitFile2Id(file2.getCommitId());

                                //                            out.printLog("Pair file already exists: " + file1.getFilePath() + " - " + file2.getFilePath());
                            } else {
                                FilePairOutput filePairOutput = new FilePairOutput(filePair);
                                filePairOutput.addIssueId(issue.getId());
                                if ("Bug".equals(issue.getType())) {
                                    filePairOutput.addDefectIssueId(issue.getId());
                                }
                                if (file1.getCommitId().equals(file2.getCommitId())) {
                                    filePairOutput.addCommitId(file1.getCommitId());
                                }
                                filePairOutput.addCommitFile1Id(file1.getCommitId());
                                filePairOutput.addCommitFile2Id(file2.getCommitId());
                                pairFiles.put(filePair, filePairOutput);
                                out.printLog("Paired file: " + file1.getFilePath() + " - " + file2.getFilePath());
                            }
                            numberPairFilesInIssue++;
                        }
                    }
                }

                numberFilePairs += numberPairFilesInIssue;
                out.printLog("Issue pairs files: " + numberPairFilesInIssue);

            }

            out.printLog("Number of pairs files: " + numberFilePairs);
            out.printLog("Result: " + pairFiles.size());

            out.printLog("Counting future defects...");
            final int total = pairFiles.keySet().size();
            int progressCountFutureDefects = 0;
            for (FilePair fileFile : pairFiles.keySet()) {
                if (++progressCountFutureDefects % 10 == 0
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

            Set<Integer> allIssues = new HashSet<>();
            for (Map.Entry<FilePair, FilePairOutput> entrySet : pairFiles.entrySet()) {
                FilePairOutput value = entrySet.getValue();
                allIssues.addAll(value.getIssuesId());
            }
            // calculando o apriori
            out.printLog("Calculing apriori...");
            out.printLog("Issues index " + index + " (x 100): " + allIssues.size());
            int totalApriori = pairFiles.size();
            int countApriori = 0;

            final List<FilePairOutput> pairFileList = new ArrayList<>();

            for (FilePair fileFile : pairFiles.keySet()) {
                if (++countApriori % 100 == 0
                        || countApriori == totalApriori) {
                    System.out.println(countApriori + "/" + totalApriori);
                }

                Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), issues);
                Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), issues);

                FilePairOutput filePairOutput = pairFiles.get(fileFile);

                FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                        filePairOutput.getIssuesIdWeight(), allIssues.size());

                fileFile.orderFilePairByConfidence(apriori);
                filePairOutput.setFilePairApriori(apriori);

                pairFileList.add(filePairOutput);
            }
            orderByFilePairSupportAndNumberOfDefects(pairFileList);

            EntityMatrix matrix = new EntityMatrix();
            matrix.setNodes(objectsToNodes(pairFileList, FilePairOutput.getToStringHeader()));
            matrix.setAdditionalFilename(String.valueOf(index + 1));
            matrix.getParams().put("index", index);
            matricesToSave.add(matrix);
        }
    }

    private void orderByFilePairSupportAndNumberOfDefects(final List<FilePairOutput> pairFileList) {
        // order by number of defects (lower priority)
        orderByNumberOfDefects(pairFileList);
        // order by support (higher priority)
        orderByFilePairSupport(pairFileList);
    }

    private void orderByFilePairSupport(final List<FilePairOutput> pairFileList) {
        Collections.sort(pairFileList, new Comparator<FilePairOutput>() {

            @Override
            public int compare(FilePairOutput o1, FilePairOutput o2) {
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

    private void orderByNumberOfDefects(final List<FilePairOutput> pairFileList) {
        Collections.sort(pairFileList, new Comparator<FilePairOutput>() {

            @Override
            public int compare(FilePairOutput o1, FilePairOutput o2) {
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
