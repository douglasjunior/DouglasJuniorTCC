package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserIssueDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.util.MathUtils;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author douglas
 */
public class BichoUserCommentedSamePairOfFileOnIssueInDateServices extends AbstractBichoMatrixServices {

    private Integer maxFilesPerCommit = 0;

    public BichoUserCommentedSamePairOfFileOnIssueInDateServices() {
        super(null, null);
    }

    public BichoUserCommentedSamePairOfFileOnIssueInDateServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoUserCommentedSamePairOfFileOnIssueInDateServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private Integer getMaxFilesPerCommit() {
        return maxFilesPerCommit;//Util.stringToInteger(params.get("maxFilesPerCommit") + "");
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

        Map<AuxUserFileFileUserDirectional, AuxUserFileFileUserDirectional> result = new HashMap<>();

//        Pattern fileToConsiders = null;
//        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
//            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
//        }
//        Pattern fileToIgnore = MatcherUtils.createExcludeMatcher(getFilesToIgnore());

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository());

        Map<Integer, Integer> numFilesPerCommit = bichoDAO.countFilesPerCommit(beginDate, endDate, true);
        maxFilesPerCommit = MathUtils.median(numFilesPerCommit.values());

        out.printLog("Median of files per commit: " + maxFilesPerCommit);

        // select a issue/pullrequest commenters
        Map<Integer, List<Integer>> issuesCommits = bichoDAO.selectIssues(
                beginDate, endDate, maxFilesPerCommit, isOnlyFixed());
        
        out.printLog("Issues (filtered): " + issuesCommits.size());

        int count = 1;
        int totalFilePairsCount = 0;

        for (Map.Entry<Integer, List<Integer>> entrySet : issuesCommits.entrySet()) {
            Integer issue = entrySet.getKey();
            List<Integer> commits = entrySet.getValue();

            out.printLog("##################### NR: " + issue);
            out.printLog(count + " of the " + issuesCommits.size());

            out.printLog(commits.size() + " commits references the issue");

            // monta os pares com os arquivos de todos os commits da issue
            List<FilePath> commitedFiles = new ArrayList<>();
            for (Integer commit : commits) {

                // select name of commited files
                List<FilePath> files = bichoDAO.selectFilesByCommitId(commit, getMaxFilesPerCommit());
                out.printLog(files.size() + " files in commit #" + commit);
                commitedFiles.addAll(files);
            }

            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

            Set<AuxFileFile> pairFiles = new HashSet<>();
            int totalPullRequestFilePairsCount = 0;
            for (int i = 0; i < commitedFiles.size(); i++) {
                FilePath file1 = commitedFiles.get(i);
                for (int j = i + 1; j < commitedFiles.size(); j++) {
                    FilePath file2 = commitedFiles.get(j);
                    if (!file1.equals(file2)
                            && !Util.stringEquals(file1.getFilePath(), file2.getFilePath())) {
                        AuxFileFile fileFile = new AuxFileFile(file1.getFilePath(),
                                file2.getFilePath());
                        if (!pairFiles.contains(fileFile)) {
                            pairFiles.add(fileFile);

                            totalPullRequestFilePairsCount++;
                        }
                    }
                }
            }
            totalFilePairsCount += totalPullRequestFilePairsCount;
            out.printLog("Issue files pairs: " + totalPullRequestFilePairsCount);

            // seleciona os autores de cada comentario (mesmo repetido)
            List<Commenter> commenters = bichoDAO.selectCommentersByIssueId(issue);
            out.printLog("Issue comments" + commenters.size());

            Map<AuxUserUserDirectional, AuxUserUserDirectional> pairCommenter
                    = new HashMap<>();

            for (int k = 0; k < commenters.size(); k++) {
                Commenter author1 = commenters.get(k);
                for (int l = k - 1; l >= 0; l--) {
                    Commenter author2 = commenters.get(l);
                    AuxUserUserDirectional pair = new AuxUserUserDirectional(
                            author1.getName(),
                            author1.getEmail(),
                            author2.getName(),
                            author2.getEmail());
                    if (pairCommenter.containsKey(pair)) {
                        pairCommenter.get(pair).inc();
                    } else {
                        pairCommenter.put(pair, pair);
                    }
                }
            }
            commenters.clear();
            out.printLog("Creating matrix of users (" + pairCommenter.size()
                    + ") and pair file (" + pairFiles.size() + ")");
            for (AuxUserUserDirectional users : pairCommenter.values()) {
                for (AuxFileFile files : pairFiles) {
                    AuxUserFileFileUserIssueDirectional aux = new AuxUserFileFileUserIssueDirectional(
                            users.getUser(),
                            files.getFileName(),
                            files.getFileName2(),
                            users.getUser2(),
                            issue,
                            users.getWeigth());

                    if (result.containsKey(aux)) {
                        result.get(aux).inc();
                    } else {
                        result.put(aux, aux);
                    }
                }
            }

            count++;
            out.printLog("Temp user result: " + result.size());
        }
        out.printLog("Number of pair files: " + totalFilePairsCount);
        out.printLog("Result: " + result.size());

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(result.values()));
        matricesToSave.add(matrix);
    }

    @Override
    public String getHeadCSV() {
        return "user;file;file2;user2;weigth";
    }
}