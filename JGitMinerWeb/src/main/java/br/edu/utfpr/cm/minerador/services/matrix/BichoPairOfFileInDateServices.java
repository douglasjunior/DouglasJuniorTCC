package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public BichoPairOfFileInDateServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
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

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        Date beginDate = getBeginDate();
        Date endDate = getEndDate();

        Map<AuxFileFile, AuxFileFile> pairFiles = new HashMap<>();

//        Pattern fileToConsiders = null;
//        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
//            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
//        }

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());

        out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
        out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

        // select a issue/pullrequest commenters
        Map<Integer, List<Integer>> issuesCommits = bichoDAO.selectIssues(
                beginDate, endDate, getMaxFilesPerCommit(), isOnlyFixed());
        
        out.printLog("Issues (filtered): " + issuesCommits.size());

        int count = 1;
        int numberFilePairs = 0;

        // combina em pares todos os arquivos commitados em uma issue
        for (Map.Entry<Integer, List<Integer>> entrySet : issuesCommits.entrySet()) {
            Integer issue = entrySet.getKey();
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

            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

            int numberPairFilesInIssue = 0;
            for (int i = 0; i < commitedFiles.size(); i++) {
                FilePath file1 = commitedFiles.get(i);
                for (int j = i + 1; j < commitedFiles.size(); j++) {
                    FilePath file2 = commitedFiles.get(j);
                    AuxFileFile fileFile = new AuxFileFile(file1.getFilePath(),
                            file2.getFilePath());
                    if (pairFiles.containsKey(fileFile)) {
                        pairFiles.get(fileFile).addIssueId(issue);
                    } else {
                        fileFile.addIssueId(issue);
                        fileFile.addCommitId(file1.commitId);
                        fileFile.addCommitId(file2.commitId);
                        pairFiles.put(fileFile, fileFile);
                    }
                }
            }
            numberFilePairs += numberPairFilesInIssue;
            out.printLog("Issue pairs files: " + numberPairFilesInIssue);

        }
        out.printLog("Number of pairs files: " + numberFilePairs);
        out.printLog("Result: " + pairFiles.size());

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFiles.values()));
        matricesToSave.add(matrix);
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;issueWeigth;issues;commitsWeight;commmits";
    }
}
