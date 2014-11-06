package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserIssueDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.util.MatcherUtils;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 
 * @author douglas
 */
public class BichoUserCommentedSamePairOfFileOnIssueInDateServices extends AbstractBichoMatrixServices {

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

        Map<AuxUserFileFileUserDirectional, AuxUserFileFileUserDirectional> result = new HashMap<>();

        Pattern fileToConsiders = null;
        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
        }
        //Pattern fileToIgnore = MatcherUtils.createExcludeMatcher(getFilesToIgnore());

        final List<Object> paramValues = new ArrayList<>();
        paramValues.add(beginDate);
        paramValues.add(endDate);

        String selectFixedIssues
                = QueryUtils.getQueryForDatabase("SELECT i.id, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE s.num_files <= " + getMaxFilesPerCommit(), getRepository());

        if (isOnlyFixed()) {
            selectFixedIssues
                    += " AND i.resolution = 'Fixed'";
        }

        selectFixedIssues += " AND i.submitted_on BETWEEN ? AND ? ";

        // select a issue/pullrequest commenters
        List<Object[]> filteredIssues = dao.selectNativeWithParams(selectFixedIssues,
                paramValues.toArray());

        Map<Integer, List<Integer>> issuesCommits = new HashMap<>();
        for (Object[] issueCommit : filteredIssues) {
            Integer issue = (Integer) issueCommit[0];
            Integer commit = (Integer) issueCommit[1];

            if (issuesCommits.containsKey(issue)) {
                issuesCommits.get(issue).add(commit);
            } else {
                List<Integer> commits = new ArrayList<>();
                commits.add(commit);
                issuesCommits.put(issue, commits);
            }
        }
        
        out.printLog("Issues: " + filteredIssues.size());

        final String selectComments
                = QueryUtils.getQueryForDatabase("SELECT p.name, p.email"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE c.issue_id = ?"
                        + " ORDER BY c.submitted_on ASC", getRepository());

        final String selectFiles
                = QueryUtils.getQueryForDatabase("SELECT fill.file_path"
                        + "  FROM {0}_vcs.files fil"
                        + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(fill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + " WHERE s.id = ?"
                        + "   AND s.num_files <= ?", getRepository());

        int count = 1;
        int totalFilePairsCount = 0;

        for (Map.Entry<Integer, List<Integer>> entrySet : issuesCommits.entrySet()) {
            Integer issue = entrySet.getKey();
            List<Integer> commits = entrySet.getValue();

            out.printLog("##################### NR: " + issue);
            out.printLog(count + " of the " + issuesCommits.size());

            out.printLog(commits.size() + " commits references the issue");

            // precisa do id do scmlog para parear os arquivos
            // precisa do id da issue para combinar o par de arquivo + par de devs
            //
            // montar os pares com os arquivos
            // de todos os commits da issue ou apenas os arquivos de um commit?
            // estou considerando que os pares serão montado pelos commits
            // com referencia a issue
            List<String> commitedFiles = new ArrayList<>();
            for (Integer commit : commits) {

                // select name of commited files
                List<String> filesName = dao.selectNativeWithParams(selectFiles,
                        new Object[]{commit, getMaxFilesPerCommit()});
                out.printLog(filesName.size() + " files in commit #" + commit);
                for (String fileName : filesName) {
//                    if (fileToConsiders == null || fileToConsiders.matcher(fileName).matches()) {
                        commitedFiles.add(fileName);
//                    }
                }
            }

            out.printLog("Number of files commited and related with issue: " + commitedFiles.size());

            Set<AuxFileFile> tempResultFiles = new HashSet<>();
            int totalPullRequestFilePairsCount = 0;
            for (int i = 0; i < commitedFiles.size(); i++) {
                String file1 = commitedFiles.get(i);
                for (int j = i + 1; j < commitedFiles.size(); j++) {
                    String file2 = commitedFiles.get(j);
                    if (!file1.equals(file2)
                            && !Util.stringEquals(file1, file2)) {
                        AuxFileFile fileFile = new AuxFileFile(file1, file2);
                        if (!tempResultFiles.contains(fileFile)) {
                            tempResultFiles.add(fileFile);

                            totalPullRequestFilePairsCount++;
                        }
                    }
                }
            }
            totalFilePairsCount += totalPullRequestFilePairsCount;
            commitedFiles = null;
            out.printLog("Issue files pairs: " + totalPullRequestFilePairsCount);

            // seleciona os autores de cada comentario (mesmo repetido)
            List<Object[]> commenters = dao.selectNativeWithParams(selectComments,
                    new Object[]{issue});
            out.printLog("Issue comments" + commenters.size());

            Map<AuxUserUserDirectional, AuxUserUserDirectional> tempResultUsers
                    = new HashMap<>();

            for (int k = 0; k < commenters.size(); k++) {
//                if (k % 1000 == 0 || k == commenters.size() - 1) {
//                    System.out.println(k + "/" + commenters.size());
//                }
                Object[] author1 = commenters.get(k);
                String authorName1 = (String) author1[0];
                String authorEmail1 = (String) author1[1];
                for (int l = k - 1; l >= 0; l--) {
                    Object[] author2 = commenters.get(l);
                    String authorName2 = (String) author2[0];
                    String authorEmail2 = (String) author2[1];
                    AuxUserUserDirectional aux = new AuxUserUserDirectional(
                            authorName1, // name
                            authorEmail1, // email
                            authorName2, // name
                            authorEmail2); // email
                    if (tempResultUsers.containsKey(aux)) {
                        tempResultUsers.get(aux).inc();
                    } else {
                        tempResultUsers.put(aux, aux);
                    }
                }
            }
            commenters.clear();
            out.printLog("Creating matrix of users (" + tempResultUsers.size()
                    + ") and pair file (" + tempResultFiles.size() + ")");
            for (AuxUserUserDirectional users : tempResultUsers.values()) {
                for (AuxFileFile files : tempResultFiles) {
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
