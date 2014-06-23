package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.FileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserDirectional;
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
public class UserCommentedSamePairOfFileInDateServices extends AbstractMatrixServices {

    public UserCommentedSamePairOfFileInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public UserCommentedSamePairOfFileInDateServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private Integer getMaxFilesPerCommit() {
        return Util.stringToInteger(params.get("maxFilesPerCommit") + "");
    }

    private Integer getMinFilesPerCommit() {
        return Util.stringToInteger(params.get("minFilesPerCommit") + "");
    }

    private boolean isOnlyMerged() {
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
        FileDAO fileDAO = new FileDAO(dao);

        StringBuilder jpql = new StringBuilder();

        Map<AuxUserFileFileUserDirectional, AuxUserFileFileUserDirectional> result = new HashMap<>();

        Pattern fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
        //Pattern fileToIgnore = MatcherUtils.createExcludeMatcher(getFilesToIgnore());

        final List<String> paramNames = new ArrayList<>();
        paramNames.add("repo");
        paramNames.add("beginDate");
        paramNames.add("endDate");

        final List<Object> paramValues = new ArrayList<>();
        paramValues.add(getRepository());
        paramValues.add(beginDate);
        paramValues.add(endDate);
        
        jpql.append("SELECT DISTINCT i")
            .append(" FROM")
            .append(" EntityPullRequest p JOIN p.issue i")
            .append(" WHERE")
            .append(" p.repository = :repo")
            .append(" AND p.createdAt BETWEEN :beginDate AND :endDate")
            .append(" AND i.commentsCount > 1");

        if (isOnlyMerged()) {
            jpql.append(" AND p.mergedAt IS NOT NULL");
        }

        System.out.println(jpql);

        // select a issue/pullrequest comments
        List<EntityIssue> issuesCommenteds = dao.selectWithParams(jpql.toString(),
                paramNames.toArray(new String[paramNames.size()]),
                paramValues.toArray());
        
        out.printLog("Issues comentadas: " + issuesCommenteds.size());

        final String selectPullRequests = "SELECT p "
                + " FROM EntityPullRequest p "
                + " WHERE p.repository = :repo "
                + (isOnlyMerged() ? " AND p.mergedAt IS NOT NULL " : "") // merged
                + " AND p.createdAt BETWEEN :beginDate AND :endDate"
                + " AND p.issue = :issue "
                + " ORDER BY p.createdAt ";

        final String[] selectPullRequestsParams = new String[]{"repo", "beginDate", "endDate", "issue"};

        final String selectComments = "SELECT c "
                + " FROM EntityComment c "
                + " WHERE c.issue = :issue "
                + " ORDER BY c.createdAt ";

        final String[] selectCommentsParams = new String[]{"issue"};

        int count = 1;
        int realPairFilesCount = 0;

        for (EntityIssue issue : issuesCommenteds) {
            out.printLog("##################### NR: " + issue.getNumber() + " URL: " + issue.getUrl());
            out.printLog(count + " of the " + issuesCommenteds.size());

            EntityPullRequest pr = dao.selectOneWithParams(selectPullRequests,
                    selectPullRequestsParams,
                    new Object[]{getRepository(), beginDate, endDate, issue});

            out.printLog("Pull Request #" + pr.getId());
            if (pr.getRepositoryCommits().isEmpty()) {
                out.printLog("No Commits in Pull Request");
                count++;
                continue;
            }

            out.printLog(pr.getRepositoryCommits().size() + " commits in pull request ");

            List<EntityCommitFile> commitFiles = new ArrayList<>();
            for (EntityRepositoryCommit comm : pr.getRepositoryCommits()) {
                if (comm.getFiles().size() <= getMaxFilesPerCommit()) {
                    for (EntityCommitFile entityCommitFile : comm.getFiles()) {
                        long countPullRequestIn = fileDAO.calculeNumberOfPullRequestWhereFileIsIn(
                                getRepository(), entityCommitFile.getFilename(),
                                beginDate, endDate, 0, getMaxFilesPerCommit(), isOnlyMerged());
                        if (//!fileToIgnore.matcher(file2.getFilename()).matches() &&
                                fileToConsiders.matcher(entityCommitFile.getFilename()).matches()
                                && countPullRequestIn > 1) {
                            commitFiles.add(entityCommitFile);
                        }// else {
                        //    out.printLog("Excluding file " + entityCommitFile.getFilename() + " #PR=" + countPullRequestIn);
                        //}
                    }
                }// else {
                //    out.printLog("Excluding Commit #" + comm.getId());
                //}
            }

            out.printLog("Number of files in pull request: " + commitFiles.size());

            Set<AuxFileFile> tempResultFiles = new HashSet<>();
            int pullRequestPairFileCount = 0;
            for (int i = 0; i < commitFiles.size(); i++) {
//                if (i % 50 == 0 || i == commitFiles.size()) {
//                    System.out.println(i + 1 + "/" + commitFiles.size());
//                }
                EntityCommitFile file1 = commitFiles.get(i);
                for (int j = i + 1; j < commitFiles.size(); j++) {
                    EntityCommitFile file2 = commitFiles.get(j);
                    if (!file1.equals(file2)
                            && !Util.stringEquals(file1.getFilename(), file2.getFilename())) {
                        AuxFileFile fileFile = new AuxFileFile(file1.getFilename(), file2.getFilename());
                        if (!tempResultFiles.contains(fileFile)) {
                            tempResultFiles.add(fileFile);

                            realPairFilesCount++;
                            pullRequestPairFileCount++;
                        }
                    }
                }
            }
            commitFiles.clear();
            out.printLog("Pull Request pair files: " + pullRequestPairFileCount);

            List<EntityComment> comments = dao.selectWithParams(selectComments,
                    selectCommentsParams,
                    new Object[]{issue});
            out.printLog(comments.size() + " comments");

            Map<AuxUserUserDirectional, AuxUserUserDirectional> tempResultUsers
                    = new HashMap<>();

            for (int k = 0; k < comments.size(); k++) {
//                if (k % 1000 == 0 || k == comments.size() - 1) {
//                    System.out.println(k + "/" + comments.size());
//                }
                EntityComment iCom = comments.get(k);
                for (int l = k - 1; l >= 0; l--) {
                    EntityComment jCom = comments.get(l);
                    if (iCom.getUser().equals(jCom.getUser())) {
                        break;
                    }
                    AuxUserUserDirectional aux = new AuxUserUserDirectional(
                            iCom.getUser().getLogin(),
                            iCom.getUser().getEmail(),
                            jCom.getUser().getLogin(),
                            jCom.getUser().getEmail());
                    if (tempResultUsers.containsKey(aux)) {
                        tempResultUsers.get(aux).inc();
                    } else {
                        tempResultUsers.put(aux, aux);
                    }
                }
            }
            comments.clear();
            out.printLog("Creating matrix of users (" + tempResultUsers.size()
                    + ") and pair file (" + tempResultFiles.size() + ")");
            for (AuxUserUserDirectional users : tempResultUsers.values()) {
                for (AuxFileFile files : tempResultFiles) {
                    AuxUserFileFileUserDirectional aux = new AuxUserFileFileUserDirectional(
                            users.getUser(),
                            files.getFileName(),
                            files.getFileName2(),
                            users.getUser2(),
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

        out.printLog("Number of pair files: " + realPairFilesCount);
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
