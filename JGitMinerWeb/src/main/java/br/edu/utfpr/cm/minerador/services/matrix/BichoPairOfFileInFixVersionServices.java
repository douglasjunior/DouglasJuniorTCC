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
public class BichoPairOfFileInFixVersionServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileInFixVersionServices() {
        super(null, null);
    }

    public BichoPairOfFileInFixVersionServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileInFixVersionServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

        String version = getVersion();
        String futureVersion = getFutureVersion();

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
        Map<Issue, List<Integer>> issuesCommits = bichoDAO.selectIssuesAndType(version);
        
        out.printLog("Issues (filtered): " + issuesCommits.size());

        int count = 1;
        int numberFilePairs = 0;
        Cacher cacher = new Cacher(bichoFileDAO);

        // combina em pares todos os arquivos commitados em uma issue
        final int totalIssues = issuesCommits.size();
        int progressFilePairing = 0;
        for (Map.Entry<Issue, List<Integer>> entrySet : issuesCommits.entrySet()) {
            if (++progressFilePairing % 10 == 0
                    || progressFilePairing == totalIssues) {
                System.out.println(progressFilePairing + "/" + totalIssues);
            }
            Issue issue = entrySet.getKey();
            List<Integer> commits = entrySet.getValue();

            out.printLog("Issue #" + issue);
            out.printLog(count++ + " of the " + issuesCommits.size());

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

        if (futureVersion != null) {
            out.printLog("Counting future defects...");
            final int total = pairFiles.keySet().size();
            int progressCountFutureDefects = 0;
            for (FilePair fileFile : pairFiles.keySet()) {
                if (++progressCountFutureDefects % 10 == 0
                        || progressCountFutureDefects == total) {
                    System.out.println(progressCountFutureDefects + "/" + total);
                }
                List<Integer> futureDefectIssues = bichoPairFileDAO.selectIssues(
                        fileFile.getFile1(), fileFile.getFile2(),
                        futureVersion, "Bug");
                pairFiles.get(fileFile).addFutureDefectIssuesId(futureDefectIssues);
            }
        }

        Set<Integer> allIssues = new HashSet<>();
        for (Map.Entry<FilePair, FilePairOutput> entrySet : pairFiles.entrySet()) {
            FilePairOutput value = entrySet.getValue();
            allIssues.addAll(value.getIssuesId());
        }
        // calculando o apriori
        out.printLog("Calculing apriori...");
        out.printLog("Issues in version " + version + ": " + allIssues.size());
        int totalApriori = pairFiles.size();
        int countApriori = 0;

        final List<FilePairOutput> pairFileList = new ArrayList<>();

        for (FilePair fileFile : pairFiles.keySet()) {
            if (++countApriori % 100 == 0
                    || countApriori == totalApriori) {
                System.out.println(countApriori + "/" + totalApriori);
            }

            Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version);
            Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version);

            FilePairOutput filePairOutput = pairFiles.get(fileFile);

            FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                    filePairOutput.getIssuesIdWeight(), allIssues.size());

            fileFile.orderFilePairByConfidence(apriori);
            filePairOutput.setFilePairApriori(apriori);

            pairFileList.add(filePairOutput);
        }
        oderByFilePairSupportAndNumberOfDefects(pairFileList);

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFileList, FilePairOutput.getToStringHeader()));
        matricesToSave.add(matrix);

//        // salvando a matriz com o top 10 par de arquivos
//        EntityMatrix matrix2 = new EntityMatrix();
//        List<FilePairOutput> top10 = getTop10(pairFileList);
//        matrix2.setNodes(objectsToNodes(top10));
//        matrix2.setAdditionalFilename(" top 10");
//        matricesToSave.add(matrix2);
//
//        // separa o top 10 em A + qualquerarquivo
//        int rank = 0;
//        for (FilePairOutput filePairTop : top10) {
//            List<FilePairOutput> changedWithA = new ArrayList<>();
//            for (FilePairOutput filePair : pairFileList) {
//                if (filePair.getFilePair().getFile1()
//                        .equals(filePairTop.getFilePair().getFile1())
//                        && !filePair.equals(filePairTop)) {
//                    changedWithA.add(filePair);
//                }
//            }
//
//            // complete with others
//            for (FilePath file : allDistinctFiles) {
//                FilePair filePair = new FilePair(filePairTop.getFilePair().getFile1(), file.getFilePath());
//                FilePairOutput combined = new FilePairOutput(filePair);
//                if (!changedWithA.contains(combined)) {
//                    changedWithA.add(combined);
//                }
//            }
//
////            filePairTop.changeToRisky();
//            changedWithA.add(0, filePairTop);
//            EntityMatrix matrix3 = new EntityMatrix();
//            matrix3.setNodes(objectsToNodes(changedWithA));
//            rank++;
//            matrix3.setAdditionalFilename(" " + rank + " file changed with " + filePairTop.getFilePair().getFile1());
//            matricesToSave.add(matrix3);
//        }
    }

    private List<FilePairOutput> getTop10(final List<FilePairOutput> pairFileList) {
        oderByFilePairSupportAndNumberOfDefects(pairFileList);

        int lastIndex = pairFileList.size() > 10 ? 10 : pairFileList.size();
        final List<FilePairOutput> top10 = pairFileList.subList(0, lastIndex);
        return top10;
    }

    private void oderByFilePairSupportAndNumberOfDefects(final List<FilePairOutput> pairFileList) {
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
