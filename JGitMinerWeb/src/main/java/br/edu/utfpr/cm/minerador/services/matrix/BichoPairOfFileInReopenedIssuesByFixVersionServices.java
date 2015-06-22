package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import static br.edu.utfpr.cm.minerador.services.matrix.AbstractBichoMatrixServices.objectsToNodes;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;

/**
 * 
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileInReopenedIssuesByFixVersionServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileInReopenedIssuesByFixVersionServices() {
        super(null, null);
    }

    public BichoPairOfFileInReopenedIssuesByFixVersionServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileInReopenedIssuesByFixVersionServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

        Map<FilePair, FilePairAprioriOutput> pairFiles = new LinkedHashMap<>();
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

        Set<Integer> allIssuesIgnored = new HashSet<>();

        // select a issue/pullrequest commenters
        Map<Issue, List<Commit>> issuesConsideredCommits = bichoDAO.selectReopenedIssuesAndType(version);
        Set<Issue> allIssues = issuesConsideredCommits.keySet();
        
        out.printLog("Issues (filtered): " + issuesConsideredCommits.size());

        int count = 1;
        Cacher cacher = new Cacher(bichoFileDAO);

        // combina em pares todos os arquivos commitados em uma issue
        final int totalIssues = issuesConsideredCommits.size();
        int progressFilePairing = 0;
        issue:
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

            // seleciona data de abertura e da 1a correcao + data de reabertura e correcao para cada vez que a issue foi reaberta
            List<Date[]> openedAndFixedDatePeriod = bichoDAO.selectIssueOpenedPeriod(issue.getId());

//            List<Date[]> intervalAtLeastOfOneDay = new ArrayList<>();
            // verifica se intervalo entre os periodos Ã© maior que 1 dia, para evitar vies
            for (Date[] openedAndFixedDatePeriod1 : openedAndFixedDatePeriod) {
                LocalDateTime opened = new LocalDateTime(openedAndFixedDatePeriod1[0].getTime());
                LocalDateTime fixed = new LocalDateTime(openedAndFixedDatePeriod1[1].getTime());
                // ignora a issue se houver um intervalo menor que 1 dia entre a abertura e a correÃ§Ã£o.
                if (Days.daysBetween(opened, fixed).getDays() < 1) {
                    allIssuesIgnored.add(issue.getId());
                    continue issue;
                }
                // considerar pelo menos 2 quando hÃ¡ 3 aberturas e 1 invalida
                // ou juntar 2 em 1 ????
//                else {
//                    intervalAtLeastOfOneDay.add(openedAndFixedDatePeriod1);
//                }
            }

//            // pode ter 3 reaberturas, pelo menos 2 tem que estar no intervalo
//            if (intervalAtLeastOfOneDay.size() < 2) {
//                continue;
//            }

            // monta os pares com os arquivos de todos os commits da issue
            Map<Integer, Set<FilePath>> commitedFiles = new LinkedHashMap<>();
            for (Commit commit : commits) {
                Integer openIndex = 0;
                boolean hasCommittedInPeriod = false; // foi encontrado entre a da de abertura e a data de correcao.
                Date commitDate = commit.getCommitDate();
                for (Date[] openedAndFixedDate : openedAndFixedDatePeriod) {
                    if (commitDate.after(openedAndFixedDate[0])
                            && commitDate.before(openedAndFixedDate[1])) {
                        hasCommittedInPeriod = true;
                        break;
                    }
                    openIndex++;
                }

                if (!hasCommittedInPeriod) {
                    continue;
                }
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
                        if (commitedFiles.containsKey(openIndex)) {
                            commitedFiles.get(openIndex).add(file);
                        } else {
                            Set<FilePath> fileSet = new HashSet<>();
                            fileSet.add(file);
                            commitedFiles.put(openIndex, fileSet);
                        }
                    }
                }
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

            pairFiles(commitedFiles, pairFiles, issue, allDefectIssues, allConsideredCommits);

        }

        out.printLog("Result: " + pairFiles.size());

        if (futureVersion != null) {
            out.printLog("Counting future issues...");
            countFutureIssues(pairFiles, bichoPairFileDAO, futureVersion);
        }

        Set<Integer> allConsideredIssues = new HashSet<>();
        for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
            FilePairOutput value = entrySet.getValue();
            allConsideredIssues.addAll(value.getIssuesId());
        }
        // calculando o apriori
        out.printLog("Calculing apriori...");
        out.printLog("Issues considered in version " + version + ": " + allConsideredIssues.size());
        int totalApriori = pairFiles.size();
        int countApriori = 0;

        final List<FilePairAprioriOutput> pairFileList = new ArrayList<>();

        for (FilePair fileFile : pairFiles.keySet()) {
            if (++countApriori % 100 == 0 || countApriori == totalApriori) {
                System.out.println(countApriori + "/" + totalApriori);
            }

            Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version);
            Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version);

            FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

            FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                    filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

            // minimum confidence is 0.5, ignore if less than 0.5
            if (apriori.getHigherConfidence() < 0.5) {
                continue;
            }

            fileFile.orderFilePairByConfidence(apriori);
            filePairOutput.setFilePairApriori(apriori);

            pairFileList.add(filePairOutput);
        }

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
                + "Number of issues ignored: " + allIssuesIgnored.size() + "\n"
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
                + "Number of issues ignored: " + allIssuesIgnored.size() + "\n"
        );

        System.out.println(Arrays.toString(allIssuesIgnored.toArray()));
    }
}
