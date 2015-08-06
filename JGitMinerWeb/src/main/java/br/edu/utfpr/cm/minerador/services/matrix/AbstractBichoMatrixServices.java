package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.AbstractBichoServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import br.edu.utfpr.cm.minerador.services.util.OrderFilePairAprioriOutputByConfidence;
import br.edu.utfpr.cm.minerador.services.util.OrderFilePairAprioriOutputByNumberOfDefects;
import br.edu.utfpr.cm.minerador.services.util.OrderFilePairAprioriOutputBySupport;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author douglas
 */
public abstract class AbstractBichoMatrixServices extends AbstractBichoServices {

    private final String repository;
    protected final List<EntityMatrix> matricesToSave;

    public AbstractBichoMatrixServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
        this.repository = null;
        this.matricesToSave = null;
    }

    public AbstractBichoMatrixServices(GenericBichoDAO dao, GenericDao genericDao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, genericDao, params, out);
        this.repository = repository;
        this.matricesToSave = matricesToSave;
    }

    public String getRepository() {
        return repository;
    }

    public void saveMatrix(EntityMatrix entityMatrix, Class<?> serviceClass) {
        out.printLog("Salvando matriz com " + entityMatrix.getNodes().size() + " registros. Parametros: " + entityMatrix.getParams());

        for (Map.Entry<Object, Object> entrySet : params.entrySet()) {
            Object key = entrySet.getKey();
            Object value = entrySet.getValue();

            if (!entityMatrix.getParams().containsKey(key)) {
                entityMatrix.getParams().put(key, value);
            }
        }
        if (entityMatrix.getRepository() == null) {
            entityMatrix.setRepository(getRepository());
        }
        entityMatrix.setClassServicesName(serviceClass.getName());
        entityMatrix.setLog(out.getLog().toString());
        for (EntityMatrixNode node : entityMatrix.getNodes()) {
            node.setMatrix(entityMatrix);
        }
        entityMatrix.setStoped(new Date());
        entityMatrix.setComplete(true);
        // saving in jgitminer database
        genericDao.insert(entityMatrix);

        out.printLog("\nSalvamento dos dados concluído!");
    }

    @Override
    public abstract void run();

    protected static List<EntityMatrixNode> objectsToNodes(Collection<? extends Object> list, String header) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        nodes.add(new EntityMatrixNode(header));
        for (Object value : list) {
            nodes.add(new EntityMatrixNode(value.toString()));
        }
        return nodes;
    }

    protected void pairFiles(Map<FilePair, FilePairAprioriOutput> pairFiles, List<FilePath> commitedFiles, Issue issue, Set<Integer> allDefectIssues, Set<Integer> allConsideredCommits) {
        for (int i = 0; i < commitedFiles.size(); i++) {
            for (int j = i + 1; j < commitedFiles.size(); j++) {
                FilePath file1 = commitedFiles.get(i);
                FilePath file2 = commitedFiles.get(j);
                if (!file1.getFilePath().equals(file2.getFilePath())) {
                    FilePair filePair = new FilePair(file1.getFilePath(), file2.getFilePath());
                    FilePairAprioriOutput filePairOutput;

                    if (pairFiles.containsKey(filePair)) {
                        filePairOutput = pairFiles.get(filePair);
                    } else {
                        filePairOutput = new FilePairAprioriOutput(filePair);
                        pairFiles.put(filePair, filePairOutput);
                    }

                    filePairOutput.addIssueId(issue.getId());
                    filePairOutput.addIssue(issue);

                    if ("Bug".equals(issue.getType())) {
                        filePairOutput.addDefectIssueId(issue.getId());
                        allDefectIssues.add(issue.getId());
                    }

                    filePairOutput.addCommitId(file1.getCommitId());
                    filePairOutput.addCommitId(file2.getCommitId());
                    // TODO refactor, replace CommitId with Commit
                    filePairOutput.addCommit(new Commit(file1.getCommitId(), null, null));
                    filePairOutput.addCommit(new Commit(file2.getCommitId(), null, null));

                    filePairOutput.addCommitFile1Id(file1.getCommitId());
                    filePairOutput.addCommitFile2Id(file2.getCommitId());

                    allConsideredCommits.add(file1.getCommitId());
                    allConsideredCommits.add(file2.getCommitId());
                }
            }
        }
    }

    protected void pairFiles(Map<Integer, Set<FilePath>> commitedFilesByIndex, Map<FilePair, FilePairAprioriOutput> pairFiles, Issue issue, Set<Integer> allDefectIssues, Set<Integer> allConsideredCommits) {
        List<Integer> openIndexes = new ArrayList<>(commitedFilesByIndex.keySet());
        Collections.sort(openIndexes);
        for (int openIndex = 0; openIndex < openIndexes.size(); openIndex++) {
            final int nextOpenIndex = openIndex + 1;

            Set<FilePath> commitedFilesI = commitedFilesByIndex.get(openIndex);
            Set<FilePath> commitedFilesJ = commitedFilesByIndex.get(nextOpenIndex);

            if ((nextOpenIndex) >= openIndexes.size()
                    || commitedFilesI == null
                    || commitedFilesJ == null) {
                break;
            }

            pairFiles(commitedFilesI, commitedFilesJ, pairFiles, issue, allDefectIssues, allConsideredCommits);
        }
    }

    protected void pairFiles(Set<FilePath> commitedFilesI, Set<FilePath> commitedFilesJ, Map<FilePair, FilePairAprioriOutput> pairFiles, Issue issue, Set<Integer> allDefectIssues, Set<Integer> allConsideredCommits) {
        for (FilePath fileI : commitedFilesI) {
            for (FilePath fileJ : commitedFilesJ) {
                if (!fileI.getFilePath().equals(fileJ.getFilePath())) {
                    FilePair filePair = new FilePair(fileI.getFilePath(), fileJ.getFilePath());
                    FilePairAprioriOutput filePairOutput;

                    if (pairFiles.containsKey(filePair)) {
                        filePairOutput = pairFiles.get(filePair);
                    } else {
                        filePairOutput = new FilePairAprioriOutput(filePair);
                        pairFiles.put(filePair, filePairOutput);
                    }

                    filePairOutput.addIssueId(issue.getId());

                    if ("Bug".equals(issue.getType())) {
                        filePairOutput.addDefectIssueId(issue.getId());
                        allDefectIssues.add(issue.getId());
                    }

                    filePairOutput.addCommitId(fileI.getCommitId());
                    filePairOutput.addCommitId(fileJ.getCommitId());

                    filePairOutput.addCommitFile1Id(fileI.getCommitId());
                    filePairOutput.addCommitFile2Id(fileJ.getCommitId());

                    allConsideredCommits.add(fileI.getCommitId());
                    allConsideredCommits.add(fileJ.getCommitId());
                }
            }
        }
    }

    protected void countFutureIssues(Map<FilePair, FilePairAprioriOutput> pairFiles, BichoPairFileDAO bichoPairFileDAO, String futureVersion) {
        final Set<FilePair> keySet = pairFiles.keySet();
        final int total = keySet.size();
        int progressCountFutureDefects = 0;
        for (FilePair fileFile : keySet) {
            if (++progressCountFutureDefects % 100 == 0 || progressCountFutureDefects == total) {
                System.out.println(progressCountFutureDefects + "/" + total);
            }
            Map<String, Set<Integer>> futureIssues = bichoPairFileDAO.selectIssues(fileFile.getFile1(), fileFile.getFile2(), futureVersion);
            final FilePairOutput pairFile = pairFiles.get(fileFile);
            Set<Integer> bugs = futureIssues.get("Bug");
            if (bugs != null) {
                pairFile.addFutureDefectIssuesId(bugs);
            }
            for (Set<Integer> issue : futureIssues.values()) {
                pairFile.addFutureIssuesId(issue);
            }
        }
    }

    protected void log(String log) {
        try {
            FileWriter w = new FileWriter(new java.io.File(System.getProperty("user.home") + "\\statistics.txt"), true);
            w.append(log);
            w.flush();
            w.close();
        } catch (Exception e) {
            System.err.println("Error to write log. " + e.getMessage());
        }
    }

    protected String generateStatistics(List<FilePairAprioriOutput> pairFileList) {
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

        for (FilePairAprioriOutput node : pairFileList) {
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

        return "Number of files: " + allFiles.size() + "\n"
                + "Number of files (JAVA): " + allJavaFiles.size() + "\n"
                + "Number of files (XML): " + allXmlFiles.size() + "\n"
                + "Number of files (Others): " + allOtherFiles.size() + "\n"
                + "Number of commits file 1: " + totalCommitsFile1.size() + "\n"
                + "Number of commits file 2: " + totalCommitsFile2.size() + "\n"
                + "Number of issues: " + totalIssues.size() + "\n"
                + "Number of commits: " + totalCommits.size() + "\n"
                + "Number of defect issues: " + totalDefects.size() + "\n"
                + "Number of future defect issues: " + totalFutureDefects.size() + "\n\n";
    }

    public void identifyFilePairs(Map<FilePair, FilePairAprioriOutput> pairFiles, BichoDAO bichoDAO, String versionString, BichoFileDAO bichoFileDAO) {
        // select a issue/pullrequest commenters
        Map<Issue, List<Commit>> issuesConsideredCommits = bichoDAO.selectIssuesAndType(versionString);
        identifyFilePairs(pairFiles, issuesConsideredCommits, bichoFileDAO);
    }

    public void identifyFilePairs(Map<FilePair, FilePairAprioriOutput> pairFiles, BichoDAO bichoDAO, BichoFileDAO bichoFileDAO) {
        // select a issue/pullrequest commenters
        Map<Issue, List<Commit>> issuesConsideredCommits = bichoDAO.selectIssuesAndType();
        identifyFilePairs(pairFiles, issuesConsideredCommits, bichoFileDAO);
    }

    public Map<FilePair, FilePairAprioriOutput> identifyFilePairs(Map<FilePair, FilePairAprioriOutput> pairFiles, Map<Issue, List<Commit>> issuesConsideredCommits, BichoFileDAO bichoFileDAO) {
        Set<FilePath> allFiles = new HashSet<>();
        Set<FilePath> allTestJavaFiles = new HashSet<>();
        Set<FilePath> allJavaFiles = new HashSet<>();
        Set<FilePath> allXmlFiles = new HashSet<>();
        Set<FilePath> allFilteredFiles = new HashSet<>();
        Set<Commit> allCommits = new HashSet<>();
        Set<Integer> allConsideredCommits = new HashSet<>();
        Set<Integer> allDefectIssues = new HashSet<>();
        if (issuesConsideredCommits.isEmpty()) {
            return pairFiles;
        }
        Set<Issue> allIssues = issuesConsideredCommits.keySet();
        out.printLog("Issues (filtered): " + issuesConsideredCommits.size());
        int count = 1;
        // combina em pares todos os arquivos commitados em uma issue
        final int totalIssues = issuesConsideredCommits.size();
        int progressFilePairing = 0;
        for (Map.Entry<Issue, List<Commit>> entrySet : issuesConsideredCommits.entrySet()) {
            if (++progressFilePairing % 100 == 0 || progressFilePairing == totalIssues) {
                System.out.println(progressFilePairing + "/" + totalIssues);
            }
            Issue issue = entrySet.getKey();
            List<Commit> commits = entrySet.getValue();
            out.printLog("Issue #" + issue);
            out.printLog(count++ + " of the " + issuesConsideredCommits.size());
            out.printLog(commits.size() + " commits references the issue");
            allCommits.addAll(commits);
            List<FilePath> commitedFiles = filterAndAggregateAllFileOfIssue(commits, bichoFileDAO, allFiles, allTestJavaFiles, allFilteredFiles, allJavaFiles, allXmlFiles);
            // empty
            if (commitedFiles.isEmpty()) {
                out.printLog("No file commited for issue #" + issue);
                continue;
            } else if (commitedFiles.size() == 1) {
                out.printLog("One file only commited for issue #" + issue);
                continue;
            }
            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());
            pairFiles(pairFiles, commitedFiles, issue, allDefectIssues, allConsideredCommits);
        }
        return pairFiles;
    }

    protected List<FilePath> filterAndAggregateAllFileOfIssue(List<Commit> commits, BichoFileDAO bichoFileDAO, Set<FilePath> allFiles, Set<FilePath> allTestJavaFiles, Set<FilePath> allFilteredFiles, Set<FilePath> allJavaFiles, Set<FilePath> allXmlFiles) {
        // monta os pares com os arquivos de todos os commits da issue
        List<FilePath> commitedFiles = new ArrayList<>();
        for (Commit commit : commits) {
            // select name of commited files
            List<FilePath> files = bichoFileDAO.selectFilesByCommitId(commit.getId());
            allFiles.addAll(files);
            out.printLog(files.size() + " files in commit #" + commit.getId());
            for (FilePath file : files) {
                if (file.getFilePath().endsWith("Test.java") || file.getFilePath().toLowerCase().endsWith("_test.java")) {
                    allTestJavaFiles.add(file);
                    allFilteredFiles.add(file);
                } else if (!file.getFilePath().endsWith(".java") && !file.getFilePath().endsWith(".xml")) {
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

    protected void orderByFilePairSupportAndConfidence(final List<FilePairAprioriOutput> pairFileList) {
        orderByFilePairSupport(pairFileList); // lower priority
        orderByFilePairConfidence(pairFileList); // higher priority
    }

    protected void orderByFilePairConfidenceAndSupport(final List<FilePairAprioriOutput> pairFileList) {
        orderByFilePairConfidence(pairFileList); // lower priority
        orderByFilePairSupport(pairFileList); // higher priority
    }

    protected void orderByFilePairSupportAndNumberOfDefects(final List<FilePairAprioriOutput> pairFileList) {
        orderByNumberOfDefects(pairFileList); // lower priority
        orderByFilePairSupport(pairFileList); // higher priority
    }

    protected void orderByNumberOfDefects(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new OrderFilePairAprioriOutputByNumberOfDefects());
    }

    protected void orderByFilePairSupport(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new OrderFilePairAprioriOutputBySupport());
    }

    protected void orderByFilePairConfidence(final List<FilePairAprioriOutput> pairFileList) {
        Collections.sort(pairFileList, new OrderFilePairAprioriOutputByConfidence());
    }
}
