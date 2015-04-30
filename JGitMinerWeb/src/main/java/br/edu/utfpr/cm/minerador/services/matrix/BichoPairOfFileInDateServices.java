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
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author douglas
 */
public class BichoPairOfFileInDateServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileInDateServices() {
        super(null, null);
    }

    public BichoPairOfFileInDateServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileInDateServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

    public Date getBeginDate() {
        return getDateParam("beginDate");
    }

    public Date getEndDate() {
        return getDateParam("endDate");
    }

    public Date getFutureBeginDate() {
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        Date beginDate = getBeginDate();
        Date endDate = getEndDate();

        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();

        Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();

//        Pattern fileToConsiders = null;
//        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
//            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
//        }

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoPairFileDAO bichoParFileDAO = new BichoPairFileDAO(dao, getRepository(), getMaxFilesPerCommit());

        out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
        out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

        // select a issue/pullrequest commenters
        Map<Integer, Set<Integer>> issuesCommits = bichoDAO.selectIssues(
                beginDate, endDate);
        
        out.printLog("Issues (filtered): " + issuesCommits.size());

        int count = 1;
        int numberFilePairs = 0;
        Cacher cacher = new Cacher(bichoFileDAO);

        // combina em pares todos os arquivos commitados em uma issue
        for (Map.Entry<Integer, Set<Integer>> entrySet : issuesCommits.entrySet()) {
            Integer issue = entrySet.getKey();
            Set<Integer> commits = entrySet.getValue();

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

            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

            int numberPairFilesInIssue = 0;
            for (int i = 0; i < commitedFiles.size(); i++) {
                FilePath file1 = commitedFiles.get(i);
                for (int j = i + 1; j < commitedFiles.size(); j++) {
                    FilePath file2 = commitedFiles.get(j);
                    if (!file1.getFilePath().equals(file2.getFilePath())) {
                        FilePair filePair = new FilePair(file1.getFilePath(), file2.getFilePath());
                        FilePairAprioriOutput filePairOutput = new FilePairAprioriOutput(filePair);
                        if (pairFiles.containsKey(filePair)) {
                            pairFiles.get(filePair).addIssueId(issue);
                        } else {
                            filePairOutput.addIssueId(issue);
                            filePairOutput.addCommitId(file1.getCommitId());
                            filePairOutput.addCommitId(file2.getCommitId());
                            pairFiles.put(filePair, filePairOutput);
                        }
                    }
                }
            }
            numberFilePairs += numberPairFilesInIssue;
            out.printLog("Issue pairs files: " + numberPairFilesInIssue);

        }
        out.printLog("Number of pairs files: " + numberFilePairs);
        out.printLog("Result: " + pairFiles.size());

        if (futureBeginDate != null
                && futureEndDate != null) {
            for (FilePair fileFile : pairFiles.keySet()) {
                List<Integer> futureDefectIssues = bichoParFileDAO.selectIssues(
                        fileFile.getFile1(), fileFile.getFile2(),
                        futureBeginDate, futureEndDate, "Bug");
                pairFiles.get(fileFile).addFutureDefectIssuesId(futureDefectIssues);
            }
        }

        // calculando o apriori
        out.printLog("Computing apriori...");

        Long allIssuesInPeriod = bichoDAO
                .calculeNumberOfIssues(beginDate, endDate, true);
        out.printLog("Issues between period: " + allIssuesInPeriod);
        int totalApriori = pairFiles.size();
        int countApriori = 0;

        final List<FilePairAprioriOutput> pairFileList = new ArrayList<>();

        for (FilePair fileFile : pairFiles.keySet()) {
            if (countApriori++ % 100 == 0) {
                System.out.println(countApriori + "/" + totalApriori);
            }

            Long file1Issues
                    = cacher.calculeNumberOfIssues(
                            fileFile.getFile1(),
                            beginDate, endDate);

            Long file2Issues
                    = cacher.calculeNumberOfIssues(
                            fileFile.getFile2(),
                            beginDate, endDate);

            FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

            FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                    filePairOutput.getIssuesIdWeight(), allIssuesInPeriod);

            filePairOutput.setFilePairApriori(apriori);

            pairFileList.add(filePairOutput);
        }

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeader()));
        matricesToSave.add(matrix);
    }

}
